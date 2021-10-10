package io.mehow.laboratory

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

internal class FeatureFactorySpec : DescribeSpec({
  val firstFactory = object : FeatureFactory {
    override fun create(): Set<Class<out Feature<*>>> = setOf(FirstFeature::class.java)
  }

  val secondFactory = object : FeatureFactory {
    override fun create(): Set<Class<out Feature<*>>> = setOf(OtherFeature::class.java)
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

