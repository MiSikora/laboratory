package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.core.spec.style.DescribeSpec
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

internal class FeatureFactoryGeneratorSpec : DescribeSpec({
  val featureA = FeatureFlagModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow", "FeatureA"),
      options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
  ).build().shouldBeRight()

  val featureB = FeatureFlagModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow", "FeatureB"),
      options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
  ).build().shouldBeRight()

  val featureC = FeatureFlagModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow.c", "FeatureA"),
      options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
  ).build().shouldBeRight()

  val factoryBuilder = FeatureFactoryModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow", "GeneratedFeatureFactory"),
      features = listOf(featureA, featureB, featureC),
  )

  describe("generated feature flag factory") {
    it("can be internal") {
      val fileSpec = factoryBuilder
          .build()
          .map { model -> model.prepare("generated").toString() }

      fileSpec shouldBeRight """
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
      val fileSpec = factoryBuilder.copy(visibility = Public)
          .build()
          .map { model -> model.prepare("generated").toString() }

      fileSpec shouldBeRight """
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
      """.trimMargin("|")
    }

    it("is optimized in case of no features") {
      val fileSpec = factoryBuilder.copy(features = emptyList())
          .build()
          .map { model -> model.prepare("generated").toString() }

      fileSpec shouldBeRight """
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
      """.trimMargin("|")
    }
  }
})
