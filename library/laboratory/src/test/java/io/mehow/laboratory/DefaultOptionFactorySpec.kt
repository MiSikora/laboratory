package io.mehow.laboratory

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

internal class DefaultOptionFactorySpec : DescribeSpec({
  val firstFactory = object : DefaultOptionFactory {
    override fun <T : Feature<out T>> create(feature: T): Feature<*>? = when (feature) {
      is FirstFeature -> FirstFeature.B
      else -> null
    }
  }

  val secondFactory = object : DefaultOptionFactory {
    override fun <T : Feature<out T>> create(feature: T): Feature<*>? = when (feature) {
      is FirstFeature -> FirstFeature.C
      is SecondFeature -> SecondFeature.C
      else -> null
    }
  }

  describe("default option factory") {
    context("when added to another") {
      val factory = (firstFactory + secondFactory)

      it("uses self as a producer if available in self") {
        factory.create(FirstFeature.A) shouldBe FirstFeature.B
      }

      it("uses another factory as a producer when unavailable in self") {
        factory.create(SecondFeature.A) shouldBe SecondFeature.C
      }

      it("uses no producer feature flag is unknown to any of factories") {
        factory.create(UnsourcedFeature.A) shouldBe null
      }
    }
  }
})
