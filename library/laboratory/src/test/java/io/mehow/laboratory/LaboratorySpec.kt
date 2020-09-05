package io.mehow.laboratory

import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.scopes.DescribeScope
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class LaboratorySpec : DescribeSpec({
  describe("laboratory") {
    it("cannot use features with no values") {
      val laboratory = Laboratory(ThrowingStorage)

      shouldThrowExactly<IllegalArgumentException> {
        laboratory.experiment<NoValuesFeature>()
      } shouldHaveMessage "io.mehow.laboratory.NoValuesFeature must have at least one value"
    }

    context("for feature with no fallback") {
      it("uses first value as a fallback") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experiment<NoFallbackFeature>() shouldBe NoFallbackFeature.A
      }

      it("uses first value as a fallback if no match is found") {
        val laboratory = Laboratory(EmptyStorage)

        laboratory.experiment<NoFallbackFeature>() shouldBe NoFallbackFeature.A
      }
    }

    context("for feature with single fallback") {
      it("uses declared fallback value") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experiment<FallbackFeature>() shouldBe FallbackFeature.B
      }

      it("uses declared fallback value if no match is found") {
        val laboratory = Laboratory(EmptyStorage)

        laboratory.experiment<FallbackFeature>() shouldBe FallbackFeature.B
      }
    }

    context("for feature with multiple fallbacks") {
      it("uses first declared fallback value") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experiment<MultiFallbackFeature>() shouldBe MultiFallbackFeature.B
      }

      it("uses first declared fallback value if no match is found") {
        val laboratory = Laboratory(EmptyStorage)

        laboratory.experiment<MultiFallbackFeature>() shouldBe MultiFallbackFeature.B
      }
    }

    val features = listOf(NoFallbackFeature::class, FallbackFeature::class, MultiFallbackFeature::class)
    for (feature in features) {
      verifyFeatureChanges(feature.java)
    }
  }

  describe("in memory laboratory") {
    it("is not shared across instances") {
      val firstLaboratory = Laboratory.inMemory()
      val secondLaboratory = Laboratory.inMemory()

      firstLaboratory.setFeature(NoFallbackFeature.B)
      firstLaboratory.experiment<NoFallbackFeature>() shouldBe NoFallbackFeature.B
      secondLaboratory.experiment<NoFallbackFeature>() shouldBe NoFallbackFeature.A

      secondLaboratory.setFeature(NoFallbackFeature.C)
      firstLaboratory.experiment<NoFallbackFeature>() shouldBe NoFallbackFeature.B
      secondLaboratory.experiment<NoFallbackFeature>() shouldBe NoFallbackFeature.C
    }
  }
})

private suspend fun <T : Feature<T>> DescribeScope.verifyFeatureChanges(feature: Class<T>) {
  context("for ${feature.simpleName!!}") {
    it("uses value saved in a storage") {
      val storage = FeatureStorage.inMemory()
      val laboratory = Laboratory(storage)

      for (value in feature.enumConstants) {
        storage.setFeature(value)

        laboratory.experiment(feature) shouldBe value
      }
    }

    it("can directly change the feature") {
      val laboratory = Laboratory.inMemory()

      for (value in feature.enumConstants) {
        laboratory.setFeature(value)

        laboratory.experiment(feature) shouldBe value
      }
    }
  }
}

private enum class NoFallbackFeature(override val isFallbackValue: Boolean = false) : Feature<NoFallbackFeature> {
  A,
  B,
  C,
  ;
}

private enum class FallbackFeature(override val isFallbackValue: Boolean = false) : Feature<FallbackFeature> {
  A,
  B(isFallbackValue = true),
  ;
}

private enum class MultiFallbackFeature(override val isFallbackValue: Boolean = false) : Feature<MultiFallbackFeature> {
  A,
  B(isFallbackValue = true),
  C(isFallbackValue = true),
  ;
}

private enum class NoValuesFeature : Feature<NoValuesFeature>

private object ThrowingStorage : FeatureStorage {
  override suspend fun <T : Feature<*>> getFeatureName(group: Class<T>) = fail()
  override suspend fun <T : Feature<*>> setFeature(feature: T) = fail()
}

private object NullStorage : FeatureStorage {
  override suspend fun <T : Feature<*>> getFeatureName(group: Class<T>): String? = null
  override suspend fun <T : Feature<*>> setFeature(feature: T) = fail()
}

private object EmptyStorage : FeatureStorage {
  override suspend fun <T : Feature<*>> getFeatureName(group: Class<T>) = ""
  override suspend fun <T : Feature<*>> setFeature(feature: T) = fail()
}

private fun fail(): Nothing = fail("Unexpected call")
