package io.mehow.laboratory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class FeatureFactorySpec : FunSpec() {
  enum class FeatureA : Feature<FeatureA> {
    A,
    ;

    override val defaultOption get() = A
  }

  enum class FeatureB : Feature<FeatureB> {
    A,
    ;

    override val defaultOption get() = A
  }

  init {
    val firstFactory = object : FeatureFactory {
      override fun create(): Set<Class<out Feature<*>>> = setOf(FeatureA::class.java)
    }

    val secondFactory = object : FeatureFactory {
      override fun create(): Set<Class<out Feature<*>>> = setOf(FeatureB::class.java)
    }

    context("factory created from sum") {
      val factory = firstFactory + secondFactory

      test("return features available in all sub-factories") {
        factory.create() shouldContainExactlyInAnyOrder setOf(
          FeatureA::class.java,
          FeatureB::class.java,
        )
      }
    }
  }
}
