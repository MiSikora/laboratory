package io.mehow.laboratory

import io.kotest.matchers.shouldBe
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import io.mehow.laboratory.testing.FeatureC
import org.junit.Test

class DefaultSourceFactoryTest {
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

  @Test
  fun `combined factory uses first factory`() {
    val factory = firstFactory + secondFactory

    factory.create(FeatureA.A) shouldBe Feature.Source.Remote
  }

  @Test
  fun `combined factory uses second factory`() {
    val factory = firstFactory + secondFactory

    factory.create(FeatureB.A) shouldBe Feature.Source.Remote
  }

  @Test
  fun `combined factory uses no factories`() {
    val factory = firstFactory + secondFactory

    factory.create(FeatureC.A) shouldBe null
  }
}
