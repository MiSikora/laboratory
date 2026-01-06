package io.mehow.laboratory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import io.mehow.laboratory.testing.FeatureC

class DefaultSourceFactorySpec : FunSpec() {
  init {
    val firstFactory =
      object : DefaultSourceFactory {
        override fun create(feature: Feature<*>): Feature.Source? =
          when (feature) {
            is FeatureA -> Feature.Source.Remote
            else -> null
          }
      }

    val secondFactory =
      object : DefaultSourceFactory {
        override fun create(feature: Feature<*>): Feature.Source? =
          when (feature) {
            is FeatureA -> Feature.Source.Local
            is FeatureB -> Feature.Source.Remote
            else -> null
          }
      }

    context("combined factory") {
      val factory = firstFactory + secondFactory

      test("use first factory") { factory.create(FeatureA.A) shouldBe Feature.Source.Remote }

      test("use second factory") { factory.create(FeatureB.A) shouldBe Feature.Source.Remote }

      test("use no factories") { factory.create(FeatureC.A) shouldBe null }
    }
  }
}
