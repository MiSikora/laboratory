package io.mehow.laboratory

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

internal class FeatureFactorySpec : DescribeSpec({
  val firstFactory = object : FeatureFactory {
    @Suppress("UNCHECKED_CAST")
    override fun create(): Set<Class<Feature<*>>> = setOf(FirstFeature::class.java) as Set<Class<Feature<*>>>
  }

  val secondFactory = object : FeatureFactory {
    @Suppress("UNCHECKED_CAST")
    override fun create(): Set<Class<Feature<*>>> = setOf(OtherFeature::class.java) as Set<Class<Feature<*>>>
  }

  describe("feature factory") {
    it("when added to another factory returns sum of available features") {
      (firstFactory + secondFactory).create() shouldContainExactlyInAnyOrder setOf(
          FirstFeature::class.java,
          OtherFeature::class.java,
      )
    }
  }
})

