package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mehow.laboratory.testing.DisabledBinaryFeature
import io.mehow.laboratory.testing.EnabledBinaryFeature
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import io.mehow.laboratory.testing.FeatureWithoutValues
import io.mehow.laboratory.testing.RemoteFeatureA
import io.mehow.laboratory.testing.RemoteFeatureB
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LaboratoryTest() {
  val laboratory =
    Laboratory.builder().localStorage(Storage.inMemory()).remoteStorage(Storage.inMemory()).build()

  @Test
  fun `change feature option`() = runTest {
    for (option in FeatureA::class.java.enumConstants) {
      laboratory.localStorage().setOptions(option)

      laboratory.experiment<FeatureA>() shouldBe option
    }
  }

  @Test
  fun `change multiple feature options`() = runTest {
    laboratory.localStorage().setOptions(FeatureA.C, FeatureB.B)

    laboratory.experiment<FeatureA>() shouldBe FeatureA.C
    laboratory.experiment<FeatureB>() shouldBe FeatureB.B
  }

  @Test
  fun `read binary feature`() = runTest {
    laboratory.isEnabled<EnabledBinaryFeature>().shouldBeTrue()
    laboratory.isEnabled<DisabledBinaryFeature>().shouldBeFalse()

    laboratory.setOptions(EnabledBinaryFeature.Disabled, DisabledBinaryFeature.Enabled)

    laboratory.isEnabled<EnabledBinaryFeature>().shouldBeFalse()
    laboratory.isEnabled<DisabledBinaryFeature>().shouldBeTrue()
  }

  @Test
  fun `fail to use feature with no values`() = runTest {
    val exception =
      shouldThrow<IllegalArgumentException> { laboratory.experiment<FeatureWithoutValues>() }
    exception shouldHaveMessage
      "io.mehow.laboratory.testing.FeatureWithoutValues must have at least one option"
  }

  @Test
  fun `read local feature`() = runTest {
    laboratory.experiment<FeatureA>() shouldBe FeatureA.A

    laboratory.localStorage().setOptions(FeatureA.B)
    laboratory.experiment<FeatureA>() shouldBe FeatureA.B
  }

  @Test
  fun `read remote feature`() = runTest {
    laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.A

    laboratory.remoteStorage()?.setOptions(RemoteFeatureA.B)
    laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.B
  }

  @Test
  fun `change feature to remote`() = runTest {
    laboratory.remoteStorage()?.setOptions(FeatureA.C)
    laboratory.experiment<FeatureA>() shouldBe FeatureA.A

    laboratory.localStorage().setRemoteSource<FeatureA>()
    laboratory.experiment<FeatureA>() shouldBe FeatureA.C
  }

  @Test
  fun `change feature to local`() = runTest {
    laboratory.localStorage().setOptions(RemoteFeatureA.C)
    laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.A

    laboratory.localStorage().setLocalSource<RemoteFeatureA>()
    laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.C
  }

  @Test
  fun `clear local features`() = runTest {
    laboratory.localStorage().setOptions(FeatureA.C, FeatureB.C)
    laboratory.localStorage().clear()

    laboratory.experiment<FeatureA>() shouldBe FeatureA.A
    laboratory.experiment<FeatureB>() shouldBe FeatureB.B
  }

  @Test
  fun `clear remote features`() = runTest {
    laboratory.remoteStorage()?.setOptions(RemoteFeatureA.C, RemoteFeatureB.C)
    laboratory.remoteStorage()?.clear()

    laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.A
    laboratory.experiment<RemoteFeatureB>() shouldBe RemoteFeatureB.B
  }

  @Test
  fun `observe feature`() = runTest {
    laboratory.observe<FeatureA>().test {
      awaitItem() shouldBe FeatureA.A

      laboratory.localStorage().setRemoteSource<FeatureA>()
      expectNoEvents()

      laboratory.localStorage().setLocalSource<FeatureA>()
      expectNoEvents()

      laboratory.localStorage().setOptions(FeatureA.B)
      awaitItem() shouldBe FeatureA.B

      laboratory.localStorage().setRemoteSource<FeatureA>()
      awaitItem() shouldBe FeatureA.A

      laboratory.remoteStorage()?.setOptions(FeatureA.C)
      awaitItem() shouldBe FeatureA.C
    }
  }

  @Test
  fun `observe binary feature`() = runTest {
    laboratory.observeBinary<EnabledBinaryFeature>().test {
      awaitItem().shouldBeTrue()

      laboratory.localStorage().setRemoteSource<EnabledBinaryFeature>()
      expectNoEvents()

      laboratory.localStorage().setLocalSource<EnabledBinaryFeature>()
      expectNoEvents()

      laboratory.setOptions(EnabledBinaryFeature.Disabled)
      awaitItem().shouldBeFalse()

      laboratory.localStorage().setRemoteSource<EnabledBinaryFeature>()
      awaitItem().shouldBeTrue()

      laboratory.remoteStorage()?.setOptions(EnabledBinaryFeature.Disabled)
      awaitItem().shouldBeFalse()
    }
  }
}
