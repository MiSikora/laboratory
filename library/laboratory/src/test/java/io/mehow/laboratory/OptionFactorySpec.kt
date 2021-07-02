package io.mehow.laboratory

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

internal class OptionFactorySpec : DescribeSpec({
  val firstFactory = object : OptionFactory {
    override fun create(key: String, name: String): Feature<*>? = when (key) {
      "FirstFeature" -> FirstFeature.A
      else -> null
    }
  }

  val secondFactory = object : OptionFactory {
    override fun create(key: String, name: String): Feature<*>? = when (key) {
      "FirstFeature" -> FirstFeature.B
      "SecondFeature" -> SecondFeature.B
      else -> null
    }
  }

  describe("option factory") {
    context("when added to another") {
      val factory = (firstFactory + secondFactory)

      it("uses self as a producer when feature flag is known to self") {
        factory.create("FirstFeature", "whatever") shouldBe FirstFeature.A
      }

      it("uses another factory as a producer when feature flag is known to other") {
        factory.create("SecondFeature", "whatever") shouldBe SecondFeature.B
      }

      it("uses no producer when feature flag is unknown to any of factories") {
        factory.create("UnsourcedFeature", "whatever") shouldBe null
      }
    }
  }
})
