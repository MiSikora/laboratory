package io.mehow.laboratory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DefaultOptionFactorySpec : FunSpec() {
  enum class FeatureA : Feature<FeatureA> {
    A,
    B,
    C,
    ;

    override val defaultOption get() = A
  }

  enum class FeatureB : Feature<FeatureB> {
    A,
    B,
    ;

    override val defaultOption get() = A
  }

  enum class FeatureC : Feature<FeatureC> {
    A,
    ;

    override val defaultOption get() = A
  }

  init {
    val firstFactory = object : DefaultOptionFactory {
      override fun <T : Feature<out T>> create(feature: T): Feature<*>? = when (feature) {
        is FeatureA -> FeatureA.C
        else -> null
      }
    }

    val secondFactory = object : DefaultOptionFactory {
      override fun <T : Feature<out T>> create(feature: T): Feature<*>? = when (feature) {
        is FeatureA -> FeatureA.B
        is FeatureB -> FeatureB.B
        else -> null
      }
    }

    context("factory created from sum") {
      val factory = firstFactory + secondFactory

      test("prioritizes first factory when option is available in it") {
        factory.create(FeatureA.A) shouldBe FeatureA.C
      }

      test("falls back to second factory when option is not available in first factory") {
        factory.create(FeatureB.A) shouldBe FeatureB.B
      }

      test("does not handle options unknown to any of sub-factories") {
        factory.create(FeatureC.A) shouldBe null
      }
    }
  }
}
