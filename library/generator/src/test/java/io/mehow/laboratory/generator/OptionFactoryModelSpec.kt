package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import io.mehow.laboratory.generator.GenerationFailure.DuplicateKeys
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.HIDDEN
import kotlin.DeprecationLevel.WARNING

internal class OptionFactoryModelSpec : DescribeSpec({
  val featureA = FeatureFlagModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow", "FeatureA"),
      options = listOf(FeatureFlagOption("OneA", isDefault = true), FeatureFlagOption("OneB")),
      key = "FeatureA",
  )

  val featureB = FeatureFlagModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow", "FeatureB"),
      options = listOf(FeatureFlagOption("TwoA", isDefault = true)),
  )

  val featureC = FeatureFlagModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow.c", "FeatureC"),
      options = listOf(FeatureFlagOption("ThreeA", isDefault = true), FeatureFlagOption("ThreeB")),
      key = "FeatureC",
  )

  val factoryBuilder = OptionFactoryModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow", "GeneratedOptionFactory"),
      features = listOf(featureA, featureB, featureC).map { it.build().shouldBeRight() },
  )

  describe("option factory model") {
    it("cannot have duplicate keys") {
      checkAll(
          Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
          Arb.stringPattern("[a-z](1)([a-z]{0,10})"),
      ) { first, second ->
        val builder = factoryBuilder.copy(
            features = listOf(
                featureA.copy(className = ClassName("io.mehow1", first)),
                featureA.copy(className = ClassName("io.mehow1", second)),
                featureB.copy(className = ClassName("io.mehow2", "${second}A")),
                featureB.copy(className = ClassName("io.mehow2", "${second}B")),
                featureB.copy(className = ClassName("io.mehow2", "${second}C"), key = "FeatureC"),
                featureC.copy(className = ClassName("io.mehow3", first)),
                featureC.copy(className = ClassName("io.mehow3", second)),
            ).map { it.build().shouldBeRight() }
        )

        val result = builder.build()

        result shouldBeLeft DuplicateKeys(mapOf(
            "FeatureA" to listOf("io.mehow1.$first", "io.mehow1.$second"),
            "FeatureC" to listOf("io.mehow2.${second}C", "io.mehow3.$first", "io.mehow3.$second"),
        ))
      }
    }

    it("cannot have key the same as other fqcn") {
      checkAll(
          Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
          Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
      ) { packageName, simpleName ->
        val builder = factoryBuilder.copy(
            features = listOf(
                featureA.copy(className = ClassName(packageName, simpleName), key = null),
                featureA.copy(className = ClassName("io.mehow", "SomeFeatureName"),
                    key = "$packageName.$simpleName"),
            ).map { it.build().shouldBeRight() }
        )

        val result = builder.build()

        result shouldBeLeft DuplicateKeys(mapOf(
            "$packageName.$simpleName" to listOf("$packageName.$simpleName",
                "io.mehow.SomeFeatureName"),
        ))
      }
    }
  }

  describe("generated option factory") {
    it("can be internal") {
      val fileSpec = factoryBuilder
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.c.FeatureC
        |import io.mehow.laboratory.Feature
        |import io.mehow.laboratory.OptionFactory
        |import kotlin.String
        |
        |internal fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
        |
        |private object GeneratedOptionFactory : OptionFactory {
        |  public override fun create(key: String, name: String): Feature<*>? = when (key) {
        |    "FeatureA" -> when (name) {
        |      "OneA" -> FeatureA.OneA
        |      "OneB" -> FeatureA.OneB
        |      else -> null
        |    }
        |    "FeatureC" -> when (name) {
        |      "ThreeA" -> FeatureC.ThreeA
        |      "ThreeB" -> FeatureC.ThreeB
        |      else -> null
        |    }
        |    "io.mehow.FeatureB" -> when (name) {
        |      "TwoA" -> FeatureB.TwoA
        |      else -> null
        |    }
        |    else -> null
        |  }
        |}
        |
      """.trimMargin()
    }

    it("can be public") {
      val fileSpec = factoryBuilder.copy(visibility = Public)
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.c.FeatureC
        |import io.mehow.laboratory.Feature
        |import io.mehow.laboratory.OptionFactory
        |import kotlin.String
        |
        |public fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
        |
        |private object GeneratedOptionFactory : OptionFactory {
        |  public override fun create(key: String, name: String): Feature<*>? = when (key) {
        |    "FeatureA" -> when (name) {
        |      "OneA" -> FeatureA.OneA
        |      "OneB" -> FeatureA.OneB
        |      else -> null
        |    }
        |    "FeatureC" -> when (name) {
        |      "ThreeA" -> FeatureC.ThreeA
        |      "ThreeB" -> FeatureC.ThreeB
        |      else -> null
        |    }
        |    "io.mehow.FeatureB" -> when (name) {
        |      "TwoA" -> FeatureB.TwoA
        |      else -> null
        |    }
        |    else -> null
        |  }
        |}
        |
      """.trimMargin()
    }

    it("is optimized in case of no features") {
      val fileSpec = factoryBuilder.copy(features = emptyList())
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import io.mehow.laboratory.OptionFactory
        |import kotlin.String
        |
        |internal fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
        |
        |private object GeneratedOptionFactory : OptionFactory {
        |  public override fun create(key: String, name: String): Feature<*>? = null
        |}
        |
      """.trimMargin()
    }

    it("suppresses usage of deprecated features") {
      val features = listOf(
          featureA,
          featureA.copy(
              deprecation = Deprecation("message", WARNING),
              className = ClassName("io.mehow", "FeatureB"),
              key = "FeatureB",
          ),
          featureA.copy(
              deprecation = Deprecation("message", ERROR),
              className = ClassName("io.mehow", "FeatureC"),
              key = "FeatureC",
          ),
          featureA.copy(
              deprecation = Deprecation("message", HIDDEN),
              className = ClassName("io.mehow", "FeatureD"),
              key = "FeatureD",
          ),
      ).map { it.build().shouldBeRight() }

      val fileSpec = factoryBuilder.copy(features = features)
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import io.mehow.laboratory.OptionFactory
        |import kotlin.String
        |import kotlin.Suppress
        |
        |internal fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
        |
        |private object GeneratedOptionFactory : OptionFactory {
        |  public override fun create(key: String, name: String): Feature<*>? = when (key) {
        |    "FeatureA" -> when (name) {
        |      "OneA" -> FeatureA.OneA
        |      "OneB" -> FeatureA.OneB
        |      else -> null
        |    }
        |    "FeatureB" -> @Suppress("DEPRECATION") when (name) {
        |      "OneA" -> FeatureB.OneA
        |      "OneB" -> FeatureB.OneB
        |      else -> null
        |    }
        |    "FeatureC" -> @Suppress("DEPRECATION_ERROR") when (name) {
        |      "OneA" -> FeatureC.OneA
        |      "OneB" -> FeatureC.OneB
        |      else -> null
        |    }
        |    "FeatureD" -> @Suppress("DEPRECATION_ERROR") when (name) {
        |      "OneA" -> FeatureD.OneA
        |      "OneB" -> FeatureD.OneB
        |      else -> null
        |    }
        |    else -> null
        |  }
        |}
        |
      """.trimMargin()
    }
  }
})
