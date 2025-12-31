package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LaboratorySpec : FunSpec() {
  enum class NoValuesFeature : Feature<NoValuesFeature>

  enum class FeatureA : Feature<FeatureA> {
    A,
    B,
    C;

    override val defaultOption
      get() = A
  }

  enum class FeatureB : Feature<FeatureB> {
    A,
    B;

    override val defaultOption
      get() = A
  }

  init {
    test("reads feature option saved in storage") {
      val storage = FeatureStorage.inMemory()
      val laboratory = Laboratory.create(storage)

      for (option in FeatureA::class.java.options) {
        storage.setOption(option)

        laboratory.experiment<FeatureA>() shouldBe option
        laboratory.experimentIs(option) shouldBe true
      }
    }

    test("changes feature option") {
      val laboratory = Laboratory.inMemory()

      for (option in FeatureA::class.java.options) {
        laboratory.setOption(option)

        laboratory.experiment<FeatureA>() shouldBe option
      }
    }

    test("changes options for multiple features") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOptions(FeatureA.C, FeatureB.B)

      laboratory.experiment<FeatureA>() shouldBe FeatureA.C
      laboratory.experiment<FeatureB>() shouldBe FeatureB.B
    }

    test("emits feature changes") {
      val laboratory = Laboratory.inMemory()

      laboratory.observe<FeatureA>().test {
        awaitItem() shouldBe FeatureA.A

        laboratory.setOption(FeatureA.B)
        awaitItem() shouldBe FeatureA.B

        laboratory.setOption(FeatureA.C)
        awaitItem() shouldBe FeatureA.C

        laboratory.setOption(FeatureA.C)
        expectNoEvents()

        laboratory.setOption(FeatureA.B)
        awaitItem() shouldBe FeatureA.B
      }
    }

    test("clears all features") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOptions(FeatureA.B, FeatureB.B)
      laboratory.clear()

      laboratory.experiment<FeatureA>() shouldBe FeatureA.A
      laboratory.experiment<FeatureB>() shouldBe FeatureB.A
    }

    test("does not share instances between in memory implementations") {
      val firstLaboratory = Laboratory.inMemory()
      val secondLaboratory = Laboratory.inMemory()

      firstLaboratory.setOption(FeatureA.B)
      firstLaboratory.experiment<FeatureA>() shouldBe FeatureA.B
      secondLaboratory.experiment<FeatureA>() shouldBe FeatureA.A

      secondLaboratory.setOption(FeatureA.C)
      firstLaboratory.experiment<FeatureA>() shouldBe FeatureA.B
      secondLaboratory.experiment<FeatureA>() shouldBe FeatureA.C
    }

    test("uses default option if no match is found") {
      val nullStorage =
        object : FeatureStorage {
          override fun observeFeatureName(feature: Class<out Feature<*>>): Flow<String?> =
            flowOf(null)

          override suspend fun getFeatureName(feature: Class<out Feature<*>>): String? = null

          override suspend fun setOptions(vararg options: Feature<*>) = fail("Unexpected call")

          override suspend fun clear() = fail("Unexpected call")
        }
      val laboratory = Laboratory.create(nullStorage)

      laboratory.experiment<FeatureA>() shouldBe FeatureA.A
    }

    context("default options factory") {
      val factory =
        object : DefaultOptionFactory {
          override fun <T : Feature<out T>> create(feature: T) =
            when (feature) {
              is FeatureA -> FeatureA.C
              is FeatureB -> FeatureA.C // Intentional wrong class
              else -> null
            }
        }

      val laboratory =
        Laboratory.builder()
          .featureStorage(FeatureStorage.inMemory())
          .defaultOptionFactory(factory)
          .build()

      beforeTest { laboratory.clear() }

      test("overrides default options") { laboratory.experiment<FeatureA>() shouldBe FeatureA.C }

      test("does not override changed options") {
        for (option in FeatureA::class.java.options) {
          laboratory.setOption(option)

          laboratory.experiment<FeatureA>() shouldBe option
        }
      }

      test("overrides emitted default options") {
        laboratory.observe<FeatureA>().test {
          awaitItem() shouldBe FeatureA.C

          laboratory.setOption(FeatureA.B)
          awaitItem() shouldBe FeatureA.B
        }
      }

      @Suppress("MaxLineLength")
      test("fails when provided default option uses wrong type") {
        shouldThrowExactly<IllegalStateException> {
          laboratory.experiment<FeatureB>()
        } shouldHaveMessage
          "Tried to use FeatureA.C as a default option for io.mehow.laboratory.LaboratorySpec.FeatureB"
      }
    }

    test("fails to use feature with no values") {
      val throwingStorage =
        object : FeatureStorage {
          override fun observeFeatureName(feature: Class<out Feature<*>>) = fail("Unexpected call")

          override suspend fun getFeatureName(feature: Class<out Feature<*>>) =
            fail("Unexpected call")

          override suspend fun setOptions(vararg options: Feature<*>) = fail("Unexpected call")

          override suspend fun clear() = fail("Unexpected call")
        }
      val laboratory = Laboratory.create(throwingStorage)

      shouldThrowExactly<IllegalStateException> {
        laboratory.experiment<NoValuesFeature>()
      } shouldHaveMessage
        "io.mehow.laboratory.LaboratorySpec.NoValuesFeature must have at least one option"
    }
  }
}
