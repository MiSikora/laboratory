package io.mehow.laboratory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import io.mehow.laboratory.testing.FeatureC

class DefaultOptionFactorySpec : FunSpec() {
  init {
    val firstFactory =
      object : DefaultOptionFactory {
        override fun create(feature: Feature<*>): Feature<*>? =
          when (feature) {
            is FeatureA -> FeatureA.B
            else -> null
          }
      }

    val secondFactory =
      object : DefaultOptionFactory {
        override fun create(feature: Feature<*>): Feature<*>? =
          when (feature) {
            is FeatureA -> FeatureA.C
            is FeatureB -> FeatureB.C
            else -> null
          }
      }

    context("combined factory") {
      val factory = firstFactory + secondFactory

      test("use first factory") { factory.create(FeatureA.A) shouldBe FeatureA.B }

      test("use second factory") { factory.create(FeatureB.A) shouldBe FeatureB.C }

      test("use no factories") { factory.create(FeatureC.A) shouldBe null }
    }
  }
}
