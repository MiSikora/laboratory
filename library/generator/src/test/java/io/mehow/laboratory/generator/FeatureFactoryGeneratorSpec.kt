package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.core.spec.style.DescribeSpec
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.generator.test.shouldSpecify

internal class FeatureFactoryGeneratorSpec : DescribeSpec({
  val featureA = FeatureFlagModel(
      className = ClassName("io.mehow", "FeatureA"),
      options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
  )

  val featureB = FeatureFlagModel(
      className = ClassName("io.mehow", "FeatureB"),
      options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
  )

  val featureC = FeatureFlagModel(
      className = ClassName("io.mehow.c", "FeatureA"),
      options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
  )

  describe("generated feature flag factory") {
    it("can be internal") {
      val model = FeatureFactoryModel(
          ClassName("io.mehow", "GeneratedFeatureFactory"),
          listOf(featureA, featureB, featureC),
          visibility = Internal,
      )

      val fileSpec = model.prepare("generated")

      fileSpec shouldSpecify """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import io.mehow.laboratory.FeatureFactory
        |import java.lang.Class
        |import kotlin.Suppress
        |import kotlin.collections.Set
        |import kotlin.collections.setOf
        |
        |internal fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
        |
        |private object GeneratedFeatureFactory : FeatureFactory {
        |  @Suppress("UNCHECKED_CAST")
        |  public override fun create() = setOf(
        |    Class.forName("io.mehow.FeatureA"),
        |    Class.forName("io.mehow.FeatureB"),
        |    Class.forName("io.mehow.c.FeatureA")
        |  ) as Set<Class<out Feature<*>>>
        |}
        |
      """.trimMargin()
    }

    it("can be public") {
      val model = FeatureFactoryModel(
          ClassName("io.mehow", "GeneratedFeatureFactory"),
          listOf(featureA, featureB, featureC),
          visibility = Public,
      )

      val fileSpec = model.prepare("generated")

      fileSpec shouldSpecify """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import io.mehow.laboratory.FeatureFactory
        |import java.lang.Class
        |import kotlin.Suppress
        |import kotlin.collections.Set
        |import kotlin.collections.setOf
        |
        |public fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
        |
        |private object GeneratedFeatureFactory : FeatureFactory {
        |  @Suppress("UNCHECKED_CAST")
        |  public override fun create() = setOf(
        |    Class.forName("io.mehow.FeatureA"),
        |    Class.forName("io.mehow.FeatureB"),
        |    Class.forName("io.mehow.c.FeatureA")
        |  ) as Set<Class<out Feature<*>>>
        |}
        |
      """.trimMargin()
    }

    it("is optimized in case of no features") {
      val model = FeatureFactoryModel(
          ClassName("io.mehow", "GeneratedFeatureFactory"),
          features = emptyList(),
      )

      val fileSpec = model.prepare("generated")

      fileSpec shouldSpecify """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import io.mehow.laboratory.FeatureFactory
        |import java.lang.Class
        |import kotlin.collections.emptySet
        |
        |internal fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
        |
        |private object GeneratedFeatureFactory : FeatureFactory {
        |  public override fun create() = emptySet<Class<out Feature<*>>>()
        |}
        |
      """.trimMargin()
    }
  }
})
