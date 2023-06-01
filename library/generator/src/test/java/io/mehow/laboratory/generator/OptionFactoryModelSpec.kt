package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.property.Arb
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.generator.test.shouldSpecify
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.HIDDEN
import kotlin.DeprecationLevel.WARNING

internal class OptionFactoryModelSpec : DescribeSpec({
  describe("option factory model") {
    it("features cannot have duplicate keys") {
      checkAll(
          Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
          Arb.stringPattern("[a-z](1)([a-z]{0,10})"),
      ) { first, second ->
        val features = listOf(
            FeatureFlagModel(
                className = ClassName("io.mehow1", first),
                options = listOf(FeatureFlagOption("First", isDefault = true)),
                key = "FeatureA",
            ),
            FeatureFlagModel(
                className = ClassName("io.mehow1", second),
                options = listOf(FeatureFlagOption("First", isDefault = true)),
                key = "FeatureA",
            ),
            FeatureFlagModel(
                className = ClassName("io.mehow2", "${second}A"),
                options = listOf(FeatureFlagOption("First", isDefault = true)),
            ),
            FeatureFlagModel(
                className = ClassName("io.mehow2", "${second}B"),
                options = listOf(FeatureFlagOption("First", isDefault = true)),
            ),
            FeatureFlagModel(
                className = ClassName("io.mehow2", "${second}C"),
                options = listOf(FeatureFlagOption("First", isDefault = true)),
                key = "FeatureB"
            ),
            FeatureFlagModel(
                className = ClassName("io.mehow3", first),
                options = listOf(FeatureFlagOption("First", isDefault = true)),
                key = "FeatureC",
            ),
            FeatureFlagModel(
                className = ClassName("io.mehow3", second),
                options = listOf(FeatureFlagOption("First", isDefault = true)),
                key = "FeatureB",
            ),
        )

        val exception = shouldThrow<IllegalArgumentException> {
          OptionFactoryModel(ClassName("io.mehow", "GeneratedOptionFactory"), features)
        }

        exception shouldHaveMessage """
          |Feature flags must have unique keys. Found following duplicates:
          | - FeatureA: [io.mehow1.${first}, io.mehow1.${second}]
          | - FeatureB: [io.mehow2.${second}C, io.mehow3.${second}]
        """.trimMargin()
      }
    }

    it("features cannot have keys the same as other fqcns") {
      checkAll(
          Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
          Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
      ) { packageName, simpleName ->
        val features = listOf(
            FeatureFlagModel(
                className = ClassName(packageName, simpleName),
                options = listOf(FeatureFlagOption("First", isDefault = true)),
            ),
            FeatureFlagModel(
                className = ClassName("io.mehow", "FeatureName"),
                options = listOf(FeatureFlagOption("First", isDefault = true)),
                key = "$packageName.$simpleName"
            ),
        )

        val exception = shouldThrow<IllegalArgumentException> {
          OptionFactoryModel(ClassName("io.mehow", "GeneratedOptionFactory"), features)
        }

        exception shouldHaveMessage """
          |Feature flags must have unique keys. Found following duplicates:
          | - $packageName.$simpleName: [$packageName.$simpleName, io.mehow.FeatureName]
        """.trimMargin()
      }
    }
  }

  describe("generated option factory") {
    it("can be internal") {
      val features = listOf(
          FeatureFlagModel(
              className = ClassName("io.mehow", "FeatureA"),
              options = listOf(FeatureFlagOption("OneA", isDefault = true), FeatureFlagOption("OneB")),
              key = "FeatureA",
          ),
          FeatureFlagModel(
              className = ClassName("io.mehow", "FeatureB"),
              options = listOf(FeatureFlagOption("TwoA", isDefault = true)),
          ),
          FeatureFlagModel(
              className = ClassName("io.mehow.c", "FeatureC"),
              options = listOf(FeatureFlagOption("ThreeA", isDefault = true), FeatureFlagOption("ThreeB")),
              key = "FeatureC",
          ),
      )
      val model = OptionFactoryModel(
          ClassName("io.mehow", "GeneratedOptionFactory"),
          features,
          visibility = Internal,
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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
        |  override fun create(key: String, name: String): Feature<*>? = when (key) {
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
      val features = listOf(
          FeatureFlagModel(
              className = ClassName("io.mehow", "FeatureA"),
              options = listOf(FeatureFlagOption("OneA", isDefault = true), FeatureFlagOption("OneB")),
              key = "FeatureA",
          ),
          FeatureFlagModel(
              className = ClassName("io.mehow", "FeatureB"),
              options = listOf(FeatureFlagOption("TwoA", isDefault = true)),
          ),
          FeatureFlagModel(
              className = ClassName("io.mehow.c", "FeatureC"),
              options = listOf(FeatureFlagOption("ThreeA", isDefault = true), FeatureFlagOption("ThreeB")),
              key = "FeatureC",
          ),
      )
      val model = OptionFactoryModel(
          ClassName("io.mehow", "GeneratedOptionFactory"),
          features,
          visibility = Public,
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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
        |  override fun create(key: String, name: String): Feature<*>? = when (key) {
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
      val model = OptionFactoryModel(
          ClassName("io.mehow", "GeneratedOptionFactory"),
          features = emptyList(),
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import io.mehow.laboratory.OptionFactory
        |import kotlin.String
        |
        |internal fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
        |
        |private object GeneratedOptionFactory : OptionFactory {
        |  override fun create(key: String, name: String): Feature<*>? = null
        |}
        |
      """.trimMargin()
    }

    it("suppresses usage of deprecated features") {
      val features = listOf(
          FeatureFlagModel(
              className = ClassName("io.mehow", "FeatureA"),
              options = listOf(FeatureFlagOption("OneA", isDefault = true)),
          ),
          FeatureFlagModel(
              className = ClassName("io.mehow", "FeatureB"),
              options = listOf(FeatureFlagOption("TwoA", isDefault = true)),
              deprecation = Deprecation("message", WARNING),
          ),
          FeatureFlagModel(
              className = ClassName("io.mehow", "FeatureC"),
              options = listOf(FeatureFlagOption("ThreeA", isDefault = true)),
              deprecation = Deprecation("message", ERROR),
          ),
          FeatureFlagModel(
              className = ClassName("io.mehow", "FeatureD"),
              options = listOf(FeatureFlagOption("FourA", isDefault = true)),
              deprecation = Deprecation("message", HIDDEN),
          ),
      )
      val model = OptionFactoryModel(
          ClassName("io.mehow", "GeneratedOptionFactory"),
          features,
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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
        |  override fun create(key: String, name: String): Feature<*>? = when (key) {
        |    "io.mehow.FeatureA" -> when (name) {
        |      "OneA" -> FeatureA.OneA
        |      else -> null
        |    }
        |    "io.mehow.FeatureB" -> @Suppress("DEPRECATION") when (name) {
        |      "TwoA" -> FeatureB.TwoA
        |      else -> null
        |    }
        |    "io.mehow.FeatureC" -> @Suppress("DEPRECATION_ERROR") when (name) {
        |      "ThreeA" -> FeatureC.ThreeA
        |      else -> null
        |    }
        |    "io.mehow.FeatureD" -> @Suppress("DEPRECATION_ERROR") when (name) {
        |      "FourA" -> FeatureD.FourA
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
