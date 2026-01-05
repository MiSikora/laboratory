package io.mehow.laboratory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB

class FeatureFactorySpec : FunSpec() {
  init {
    val firstFactory =
      object : FeatureFactory {
        override fun create(): Set<Class<out Feature<*>>> = setOf(FeatureA::class.java)
      }

    val secondFactory =
      object : FeatureFactory {
        override fun create(): Set<Class<out Feature<*>>> = setOf(FeatureB::class.java)
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
