package io.mehow.laboratory

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class LaboratorySpec : DescribeSpec({
  describe("laboratory") {
    it("cannot use features without values") {
      val laboratory = Laboratory(ThrowingStorage)

      shouldThrowExactly<IllegalArgumentException> {
        laboratory.experiment<InvalidFeature>()
      } shouldHaveMessage "io.mehow.laboratory.InvalidFeature must have at least one value"
    }

    it("uses first feature by default") {
      val laboratory = Laboratory(NullStorage)

      laboratory.experiment<ValidFeature>() shouldBe ValidFeature.A
    }

    it("uses first feature if no name match found") {
      val laboratory = Laboratory(EmptyStorage)

      laboratory.experiment<ValidFeature>() shouldBe ValidFeature.A
    }

    it("uses feature saved in a storage") {
      val storage = FeatureStorage.inMemory()
      val laboratory = Laboratory(storage)

      for (feature in ValidFeature.values()) {
        storage.setFeature(feature)

        laboratory.experiment<ValidFeature>() shouldBe feature
      }
    }

    it("can directly change a feature") {
      val laboratory = Laboratory.inMemory()

      for (feature in ValidFeature.values()) {
        laboratory.setFeature(feature)

        laboratory.experiment<ValidFeature>() shouldBe feature
      }
    }
  }

  describe("in memory laboratory") {
    it("is not shared across instances") {
      val firstLaboratory = Laboratory.inMemory()
      val secondLaboratory = Laboratory.inMemory()

      firstLaboratory.setFeature(ValidFeature.B)
      firstLaboratory.experiment<ValidFeature>() shouldBe ValidFeature.B
      secondLaboratory.experiment<ValidFeature>() shouldBe ValidFeature.A

      secondLaboratory.setFeature(ValidFeature.C)
      firstLaboratory.experiment<ValidFeature>() shouldBe ValidFeature.B
      secondLaboratory.experiment<ValidFeature>() shouldBe ValidFeature.C
    }
  }
})

private enum class ValidFeature {
  A, B, C
}

private enum class InvalidFeature

private object ThrowingStorage : FeatureStorage {
  override fun <T : Enum<*>> getFeatureName(group: Class<T>) = assert()
  override fun <T : Enum<*>> setFeature(feature: T) = assert()
  private fun assert(): Nothing = throw AssertionError("Test failed!")
}

private object NullStorage : FeatureStorage {
  override fun <T : Enum<*>> getFeatureName(group: Class<T>): String? = null
  override fun <T : Enum<*>> setFeature(feature: T) = Unit
}

private object EmptyStorage : FeatureStorage {
  override fun <T : Enum<*>> getFeatureName(group: Class<T>) = ""
  override fun <T : Enum<*>> setFeature(feature: T) = Unit
}
