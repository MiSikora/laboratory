package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class LaboratorySpec : DescribeSpec({
  describe("laboratory") {
    it("cannot use features with no values") {
      val laboratory = Laboratory(ThrowingStorage)

      shouldThrowExactly<IllegalStateException> {
        laboratory.experiment<NoValuesFeature>()
      } shouldHaveMessage "class io.mehow.laboratory.NoValuesFeature must have at least one option"
    }

    context("for feature with single default") {
      it("uses declared default value") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experiment<SomeFeature>() shouldBe SomeFeature.B
      }

      it("uses declared default value if no match is found") {
        val laboratory = Laboratory(EmptyStorage)

        laboratory.experiment<SomeFeature>() shouldBe SomeFeature.B
      }
    }

    context("checking feature value") {
      it("returns false for non-default value") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experimentIs(SomeFeature.A) shouldBe false
      }

      it("returns true for default value") {
        val laboratory = Laboratory(NullStorage)

        laboratory.experimentIs(SomeFeature.B) shouldBe true
      }
    }

    context("reading and writing feature flag") {
      val feature = SomeFeature::class.java

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

    it("observes feature changes") {
      val laboratory = Laboratory.inMemory()

      laboratory.observe<SomeFeature>().test {
        expectItem() shouldBe SomeFeature.B

        laboratory.setFeature(SomeFeature.A)
        expectItem() shouldBe SomeFeature.A

        laboratory.setFeature(SomeFeature.C)
        expectItem() shouldBe SomeFeature.C

        laboratory.setFeature(SomeFeature.C)
        expectNoEvents()

        laboratory.setFeature(SomeFeature.B)
        expectItem() shouldBe SomeFeature.B

        cancel()
      }
    }
  }

  describe("in memory laboratory") {
    it("is not shared across instances") {
      val firstLaboratory = Laboratory.inMemory()
      val secondLaboratory = Laboratory.inMemory()

      firstLaboratory.setFeature(SomeFeature.A)
      firstLaboratory.experiment<SomeFeature>() shouldBe SomeFeature.A
      secondLaboratory.experiment<SomeFeature>() shouldBe SomeFeature.B

      secondLaboratory.setFeature(SomeFeature.C)
      firstLaboratory.experiment<SomeFeature>() shouldBe SomeFeature.A
      secondLaboratory.experiment<SomeFeature>() shouldBe SomeFeature.C
    }
  }
})

private enum class SomeFeature : Feature<SomeFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = B
}

private enum class NoValuesFeature : Feature<NoValuesFeature>

private object ThrowingStorage : FeatureStorage {
  override fun <T : Feature<*>> observeFeatureName(feature: Class<T>) = fail("Unexpected call")
  override suspend fun <T : Feature<*>> getFeatureName(feature: Class<T>) = fail("Unexpected call")
  override suspend fun <T : Feature<*>> setFeatures(vararg options: T) = fail("Unexpected call")
}

private object NullStorage : FeatureStorage {
  override fun <T : Feature<*>> observeFeatureName(feature: Class<T>): Flow<String?> = flowOf(null)
  override suspend fun <T : Feature<*>> getFeatureName(feature: Class<T>): String? = null
  override suspend fun <T : Feature<*>> setFeatures(vararg options: T) = fail("Unexpected call")
}

private object EmptyStorage : FeatureStorage {
  override fun <T : Feature<*>> observeFeatureName(feature: Class<T>) = flowOf("")
  override suspend fun <T : Feature<*>> getFeatureName(feature: Class<T>) = ""
  override suspend fun <T : Feature<*>> setFeatures(vararg options: T) = fail("Unexpected call")
}
