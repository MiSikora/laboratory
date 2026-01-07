package io.mehow.laboratory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB

class OptionFactorySpec : FunSpec() {
  init {
    val firstFactory =
      object : OptionFactory {
        override fun create(key: String, name: String): Feature<*>? =
          when (key) {
            "FeatureA" -> FeatureA.A
            else -> null
          }

        override fun create(key: String, binaryValue: Boolean): Feature<*>? =
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

        override fun create(key: String, binaryValue: Boolean): Feature<*>? =
          when (key) {
            "FeatureA" -> FeatureA.B
            "FeatureB" -> FeatureB.B
            else -> null
          }
      }

    context("combined factory") {
      val factory = firstFactory + secondFactory

      test("use first factory") {
        factory.create("FeatureA", "") shouldBe FeatureA.A
        factory.create("FeatureA", true) shouldBe FeatureA.A
      }

      test("use second factory") {
        factory.create("FeatureB", "") shouldBe FeatureB.B
        factory.create("FeatureB", true) shouldBe FeatureB.B
      }

      test("use no factories") { factory.create("Unknown", true) shouldBe null }
    }
  }
}
