package io.mehow.laboratory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB

class FeatureFactorySpec : FunSpec() {
  init {
    val firstFactory =
      object : FeatureFactory {
        @Suppress("UNCHECKED_CAST")
        override fun create() = setOf(FeatureA::class.java) as Set<Class<Feature<*>>>
      }

    val secondFactory =
      object : FeatureFactory {
        @Suppress("UNCHECKED_CAST")
        override fun create() = setOf(FeatureB::class.java) as Set<Class<Feature<*>>>
      }

    context("combined factory") {
      val factory = firstFactory + secondFactory

      test("return all features") {
        factory.create() shouldContainExactlyInAnyOrder
          setOf(FeatureA::class.java, FeatureB::class.java)
      }
    }
  }
}
