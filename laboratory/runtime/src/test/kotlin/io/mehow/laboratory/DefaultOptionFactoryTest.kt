package io.mehow.laboratory

import io.kotest.matchers.shouldBe
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import io.mehow.laboratory.testing.FeatureC
import org.junit.Test

class DefaultOptionFactoryTest() {
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

  @Test
  fun `combined factory uses first factory`() {
    val factory = firstFactory + secondFactory

    factory.create(FeatureA.A) shouldBe FeatureA.B
  }

  @Test
  fun `combined factory uses second factory`() {
    val factory = firstFactory + secondFactory

    factory.create(FeatureB.A) shouldBe FeatureB.C
  }

  @Test
  fun `combined factory uses no factories`() {
    val factory = firstFactory + secondFactory

    factory.create(FeatureC.A) shouldBe null
  }
}
