package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class LaboratorySpec : DescribeSpec({
  val throwingStorage = object : FeatureStorage {
    override fun observeFeatureName(feature: Class<out Feature<*>>) = fail("Unexpected call")
    override suspend fun getFeatureName(feature: Class<out Feature<*>>) = fail("Unexpected call")
    override suspend fun setOptions(vararg options: Feature<*>) = fail("Unexpected call")
    override suspend fun clear() = fail("Unexpected call")
  }

  val nullStorage = object : FeatureStorage {
    override fun observeFeatureName(feature: Class<out Feature<*>>): Flow<String?> = flowOf(null)
    override suspend fun getFeatureName(feature: Class<out Feature<*>>): String? = null
    override suspend fun setOptions(vararg options: Feature<*>) = fail("Unexpected call")
    override suspend fun clear() = fail("Unexpected call")
  }

  val emptyStorage = object : FeatureStorage {
    override fun observeFeatureName(feature: Class<out Feature<*>>) = flowOf("")
    override suspend fun getFeatureName(feature: Class<out Feature<*>>) = ""
    override suspend fun setOptions(vararg options: Feature<*>) = fail("Unexpected call")
    override suspend fun clear() = fail("Unexpected call")
  }

  describe("laboratory") {
    it("cannot use features with no values") {
      val laboratory = Laboratory.create(throwingStorage)

      shouldThrowExactly<IllegalStateException> {
        laboratory.experiment<NoValuesFeature>()
      } shouldHaveMessage "io.mehow.laboratory.NoValuesFeature must have at least one option"
    }

    context("for feature with single default") {
      it("uses declared default value") {
        val laboratory = Laboratory.create(nullStorage)

        laboratory.experiment<SomeFeature>() shouldBe SomeFeature.B
      }

      it("uses declared default value if no match is found") {
        val laboratory = Laboratory.create(emptyStorage)

        laboratory.experiment<SomeFeature>() shouldBe SomeFeature.B
      }
    }

    context("checking feature value") {
      it("returns false for non-default value") {
        val laboratory = Laboratory.create(nullStorage)

        laboratory.experimentIs(SomeFeature.A) shouldBe false
      }

      it("returns true for default value") {
        val laboratory = Laboratory.create(nullStorage)

        laboratory.experimentIs(SomeFeature.B) shouldBe true
      }
    }

    context("reading and writing feature flag") {
      val feature = SomeFeature::class.java

      it("uses value saved in a storage") {
        val storage = FeatureStorage.inMemory()
        val laboratory = Laboratory.create(storage)

        for (value in feature.options) {
          storage.setOption(value)

          laboratory.experiment(feature) shouldBe value
        }
      }

      it("can directly change the feature") {
        val laboratory = Laboratory.inMemory()

        for (value in feature.options) {
          laboratory.setOption(value)

          laboratory.experiment(feature) shouldBe value
        }
      }
    }

    it("observes feature changes") {
      val laboratory = Laboratory.inMemory()

      laboratory.observe<SomeFeature>().test {
        awaitItem() shouldBe SomeFeature.B

        laboratory.setOption(SomeFeature.A)
        awaitItem() shouldBe SomeFeature.A

        laboratory.setOption(SomeFeature.C)
        awaitItem() shouldBe SomeFeature.C

        laboratory.setOption(SomeFeature.C)
        expectNoEvents()

        laboratory.setOption(SomeFeature.B)
        awaitItem() shouldBe SomeFeature.B

        cancel()
      }
    }

    it("clears all feature flags") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOption(SomeFeature.A)
      laboratory.setOption(OtherFeature.B)
      laboratory.clear()

      laboratory.experimentIs(SomeFeature.B)
      laboratory.experimentIs(OtherFeature.A)
    }
  }

  describe("in memory laboratory") {
    it("is not shared across instances") {
      val firstLaboratory = Laboratory.inMemory()
      val secondLaboratory = Laboratory.inMemory()

      firstLaboratory.setOption(SomeFeature.A)
      firstLaboratory.experiment<SomeFeature>() shouldBe SomeFeature.A
      secondLaboratory.experiment<SomeFeature>() shouldBe SomeFeature.B

      secondLaboratory.setOption(SomeFeature.C)
      firstLaboratory.experiment<SomeFeature>() shouldBe SomeFeature.A
      secondLaboratory.experiment<SomeFeature>() shouldBe SomeFeature.C
    }
  }

  describe("default options factory") {
    val factory = object : DefaultOptionFactory {
      override fun <T : Feature<out T>> create(feature: T) = when (feature) {
        is SomeFeature -> OtherFeature.C // Intentional wrong class for test
        is OtherFeature -> OtherFeature.C
        else -> null
      }
    }

    it("changes default option") {
      val laboratory = Laboratory.builder()
          .featureStorage(FeatureStorage.inMemory())
          .defaultOptionFactory(factory)
          .build()
      laboratory.experimentIs(OtherFeature.C).shouldBeTrue()
    }

    it("does not affect stored option") {
      val laboratory = Laboratory.builder()
          .featureStorage(FeatureStorage.inMemory())
          .defaultOptionFactory(factory)
          .build()
      laboratory.setOption(OtherFeature.B)
      laboratory.experimentIs(OtherFeature.B).shouldBeTrue()
    }

    it("changes default option when feature flag is observed") {
      val laboratory = Laboratory.builder()
          .featureStorage(FeatureStorage.inMemory())
          .defaultOptionFactory(factory)
          .build()
      laboratory.observe<OtherFeature>().test {
        awaitItem() shouldBe OtherFeature.C

        laboratory.setOption(OtherFeature.B)
        awaitItem() shouldBe OtherFeature.B
      }
    }

    it("fails when provided default option has wrong type") {
      val laboratory = Laboratory.builder()
          .featureStorage(FeatureStorage.inMemory())
          .defaultOptionFactory(factory)
          .build()
      shouldThrowExactly<IllegalStateException> {
        laboratory.experiment<SomeFeature>()
      } shouldHaveMessage "Tried to use OtherFeature.C as a default option for io.mehow.laboratory.SomeFeature"
    }
  }
})
