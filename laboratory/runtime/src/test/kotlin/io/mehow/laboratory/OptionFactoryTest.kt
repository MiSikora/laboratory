package io.mehow.laboratory

import io.kotest.matchers.shouldBe
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import org.junit.Test

class OptionFactoryTest() {
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

  @Test
  fun `combined factory uses first factory`() {
    val factory = firstFactory + secondFactory

    factory.create("FeatureA", "") shouldBe FeatureA.A
    factory.create("FeatureA", true) shouldBe FeatureA.A
  }

  @Test
  fun `combined factory uses second factory`() {
    val factory = firstFactory + secondFactory

    factory.create("FeatureB", "") shouldBe FeatureB.B
    factory.create("FeatureB", true) shouldBe FeatureB.B
  }

  @Test
  fun `combined factory uses no factories`() {
    val factory = firstFactory + secondFactory

    factory.create("Unknown", true) shouldBe null
  }
}
