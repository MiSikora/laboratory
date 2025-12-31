package io.mehow.laboratory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class OptionFactorySpec : FunSpec() {
  enum class FeatureA : Feature<FeatureA> {
    A,
    B;

    override val defaultOption
      get() = A
  }

  enum class FeatureB : Feature<FeatureB> {
    A,
    B;

    override val defaultOption
      get() = A
  }

  init {
    val firstFactory =
      object : OptionFactory {
        override fun create(key: String, name: String): Feature<*>? =
          when (key) {
            "FeatureA" -> FeatureA.A
            else -> null
          }
      }

    val secondFactory =
      object : OptionFactory {
        override fun create(key: String, name: String): Feature<*>? =
          when (key) {
            "FeatureA" -> FeatureA.B
            "FeatureB" -> FeatureB.B
            else -> null
          }
      }

    context("factory created from sum") {
      val factory = firstFactory + secondFactory

      test("prioritizes first factory when feature is available in it") {
        factory.create("FeatureA", "") shouldBe FeatureA.A
      }

      test("falls back to second factory when feature is not available in first factory") {
        factory.create("FeatureB", "") shouldBe FeatureB.B
      }

      test("does not handle features unknown to any of sub-factories") {
        factory.create("Unknown", "") shouldBe null
      }
    }
  }
}
