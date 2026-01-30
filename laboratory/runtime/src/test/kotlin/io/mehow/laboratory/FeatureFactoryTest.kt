package io.mehow.laboratory

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import org.junit.Test

class FeatureFactoryTest() {
  val firstFactory =
    object : FeatureFactory {
      override fun create() = setOf(FeatureA::class.java)
    }

  val secondFactory =
    object : FeatureFactory {
      override fun create() = setOf(FeatureB::class.java)
    }

  @Test
  fun `combined factory returns all features`() {
    val factory = firstFactory + secondFactory

    factory.create() shouldContainExactlyInAnyOrder
      setOf(FeatureA::class.java, FeatureB::class.java)
  }
}
