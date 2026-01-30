package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LaboratoryDefaultOptionTest() {
  val factory =
    object : DefaultOptionFactory {
      override fun create(feature: Feature<*>) =
        when (feature) {
          is FeatureA -> FeatureA.C
          is FeatureB -> FeatureA.C // Intentionally wrong class
          else -> null
        }
    }
  val laboratory =
    Laboratory.builder().localStorage(Storage.inMemory()).defaultOptionFactory(factory).build()

  @Test
  fun `override default option`() = runTest {
    laboratory.experiment<FeatureA>() shouldBe FeatureA.C
  }

  @Test
  fun `do not override changed option`() = runTest {
    for (option in FeatureA::class.java.enumConstants) {
      laboratory.localStorage().setOptions(option)

      laboratory.experiment<FeatureA>() shouldBe option
    }
  }

  @Test
  fun `override default option in flow`() = runTest {
    laboratory.observe<FeatureA>().test {
      awaitItem() shouldBe FeatureA.C

      laboratory.localStorage().setOptions(FeatureA.B)
      awaitItem() shouldBe FeatureA.B
    }
  }

  @Test
  fun `fail when provided default option uses wrong type`() = runTest {
    val exception = shouldThrow<IllegalStateException> { laboratory.experiment<FeatureB>() }
    exception shouldHaveMessage
      "Tried to use FeatureA.C as a default option for io.mehow.laboratory.testing.FeatureB"
  }
}
