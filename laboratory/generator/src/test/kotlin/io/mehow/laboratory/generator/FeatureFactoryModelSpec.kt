package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.core.spec.style.FunSpec
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.generator.test.shouldSpecify

class FeatureFactoryModelSpec :
  FunSpec({
    val featureA =
      FeatureFlagModel(
        className = ClassName("io.mehow", "FeatureA"),
        options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
      )

    val featureB =
      FeatureFlagModel(
        className = ClassName("io.mehow", "FeatureB"),
        options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
      )

    val featureC =
      FeatureFlagModel(
        className = ClassName("io.mehow.c", "FeatureA"),
        options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
      )

    test("can be internal") {
      val model =
        FeatureFactoryModel(
          ClassName("io.mehow", "GeneratedFeatureFactory"),
          listOf(featureA, featureB, featureC),
          visibility = Internal,
        )

      val fileSpec = model.prepare("generated")

      fileSpec shouldSpecify
        """
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
        |  override fun create(): Set<Class<out Feature<*>>> = setOf(
        |    Class.forName("io.mehow.FeatureA"),
        |    Class.forName("io.mehow.FeatureB"),
        |    Class.forName("io.mehow.c.FeatureA")
        |  ) as Set<Class<out Feature<*>>>
        |}
        |"""
          .trimMargin()
    }

    test("can be public") {
      val model =
        FeatureFactoryModel(
          ClassName("io.mehow", "GeneratedFeatureFactory"),
          listOf(featureA, featureB, featureC),
          visibility = Public,
        )

      val fileSpec = model.prepare("generated")

      fileSpec shouldSpecify
        """
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
        |  override fun create(): Set<Class<out Feature<*>>> = setOf(
        |    Class.forName("io.mehow.FeatureA"),
        |    Class.forName("io.mehow.FeatureB"),
        |    Class.forName("io.mehow.c.FeatureA")
        |  ) as Set<Class<out Feature<*>>>
        |}
        |"""
          .trimMargin()
    }

    test("is optimized when there are no features") {
      val model =
        FeatureFactoryModel(
          ClassName("io.mehow", "GeneratedFeatureFactory"),
          features = emptyList(),
        )

      val fileSpec = model.prepare("generated")

      fileSpec shouldSpecify
        """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import io.mehow.laboratory.FeatureFactory
        |import java.lang.Class
        |import kotlin.collections.Set
        |import kotlin.collections.emptySet
        |
        |internal fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
        |
        |private object GeneratedFeatureFactory : FeatureFactory {
        |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
        |}
        |"""
          .trimMargin()
    }
  })
