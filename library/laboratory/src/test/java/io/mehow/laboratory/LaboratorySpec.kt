package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.scopes.DescribeScope
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LaboratorySpec : DescribeSpec({
  describe("laboratory") {
    it("cannot use features with no values") {
      val laboratory = Laboratory(ThrowingStorage)

      shouldThrowExactly<IllegalArgumentException> {
        laboratory.experiment<NoValuesFeature>()
      } shouldHaveMessage "io.mehow.laboratory.NoValuesFeature must have at least one value"
    }

    context("for feature with no default") {
      it("uses first value as a default") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experiment<NoDefaultFeature>() shouldBe NoDefaultFeature.A
      }

      it("uses first value as a default if no match is found") {
        val laboratory = Laboratory(EmptyStorage)

        laboratory.experiment<NoDefaultFeature>() shouldBe NoDefaultFeature.A
      }
    }

    context("for feature with single default") {
      it("uses declared default value") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experiment<DefaultFeature>() shouldBe DefaultFeature.B
      }

      it("uses declared default value if no match is found") {
        val laboratory = Laboratory(EmptyStorage)

        laboratory.experiment<DefaultFeature>() shouldBe DefaultFeature.B
      }
    }

    context("for feature with multiple defaults") {
      it("uses first declared default value") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experiment<MultiDefaultFeature>() shouldBe MultiDefaultFeature.B
      }

      it("uses first declared default value if no match is found") {
        val laboratory = Laboratory(EmptyStorage)

        laboratory.experiment<MultiDefaultFeature>() shouldBe MultiDefaultFeature.B
      }
    }

    context("checking feature value") {
      it("returns false for non-default value") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experimentIs(DefaultFeature.A) shouldBe false
      }

      it("returns true for default value") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experimentIs(DefaultFeature.B) shouldBe true
      }
    }

    val features = listOf(NoDefaultFeature::class, DefaultFeature::class, MultiDefaultFeature::class)
    for (feature in features) {
      verifyFeatureChanges(feature.java)
    }

    it("observes feature changes") {
      val laboratory = Laboratory.inMemory()

      laboratory.observe<NoDefaultFeature>().test {
        expectItem() shouldBe NoDefaultFeature.A

        laboratory.setFeature(NoDefaultFeature.B)
        expectItem() shouldBe NoDefaultFeature.B

        laboratory.setFeature(NoDefaultFeature.A)
        expectItem() shouldBe NoDefaultFeature.A

        laboratory.setFeature(NoDefaultFeature.A)
        expectNoEvents()

        laboratory.setFeature(NoDefaultFeature.C)
        expectItem() shouldBe NoDefaultFeature.C

        cancel()
      }
    }
  }

  describe("in memory laboratory") {
    it("is not shared across instances") {
      val firstLaboratory = Laboratory.inMemory()
      val secondLaboratory = Laboratory.inMemory()

      firstLaboratory.setFeature(NoDefaultFeature.B)
      firstLaboratory.experiment<NoDefaultFeature>() shouldBe NoDefaultFeature.B
      secondLaboratory.experiment<NoDefaultFeature>() shouldBe NoDefaultFeature.A

      secondLaboratory.setFeature(NoDefaultFeature.C)
      firstLaboratory.experiment<NoDefaultFeature>() shouldBe NoDefaultFeature.B
      secondLaboratory.experiment<NoDefaultFeature>() shouldBe NoDefaultFeature.C
    }
  }
})

private suspend fun <T : Feature<T>> DescribeScope.verifyFeatureChanges(feature: Class<T>) {
  context("for ${feature.simpleName}") {
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

private enum class NoDefaultFeature(override val isDefaultValue: Boolean = false) : Feature<NoDefaultFeature> {
  A,
  B,
  C,
  ;
}

private enum class DefaultFeature(override val isDefaultValue: Boolean = false) : Feature<DefaultFeature> {
  A,
  B(isDefaultValue = true),
  ;
}

private enum class MultiDefaultFeature(override val isDefaultValue: Boolean = false) : Feature<MultiDefaultFeature> {
  A,
  B(isDefaultValue = true),
  C(isDefaultValue = true),
  ;
}

private enum class NoValuesFeature : Feature<NoValuesFeature>

private object ThrowingStorage : FeatureStorage {
  override fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>) = fail()
  override suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>) = fail()
  override suspend fun <T : Feature<*>> setFeatures(vararg features: T) = fail()
}

private object NullStorage : FeatureStorage {
  override fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>): Flow<String?> = flowOf(null)
  override suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>): String? = null
  override suspend fun <T : Feature<*>> setFeatures(vararg features: T) = fail()
}

private object EmptyStorage : FeatureStorage {
  override fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>) = flowOf("")
  override suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>) = ""
  override suspend fun <T : Feature<*>> setFeatures(vararg features: T) = fail()
}

private fun fail(): Nothing = fail("Unexpected call")
