package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.RemoteFeatureA
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LaboratoryDefaultSourceTest() {
  val factory =
    object : DefaultSourceFactory {
      override fun create(feature: Feature<*>) =
        when (feature) {
          is FeatureA -> Feature.Source.Remote
          is RemoteFeatureA -> Feature.Source.Local
          else -> null
        }
    }

  val laboratory =
    Laboratory.builder()
      .localStorage(Storage.inMemory())
      .remoteStorage(Storage.inMemory())
      .defaultSourceFactory(factory)
      .build()

  @Test
  fun `override default local source`() = runTest {
    laboratory.remoteStorage()?.setOptions(FeatureA.C)

    laboratory.experiment<FeatureA>() shouldBe FeatureA.C
  }

  @Test
  fun `override default remote source`() = runTest {
    laboratory.localStorage().setOptions(RemoteFeatureA.C)

    laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.C
  }

  @Test
  fun `do not override changed local source`() = runTest {
    laboratory.localStorage().setLocalSource<FeatureA>()
    laboratory.remoteStorage()?.setOptions(FeatureA.C)

    laboratory.experiment<FeatureA>() shouldBe FeatureA.A
  }

  @Test
  fun `do not override changed remote source`() = runTest {
    laboratory.localStorage().setRemoteSource<RemoteFeatureA>()
    laboratory.localStorage().setOptions(RemoteFeatureA.C)

    laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.A
  }

  @Test
  fun `override default source in flow`() = runTest {
    laboratory.observe<FeatureA>().test {
      awaitItem() shouldBe FeatureA.A

      laboratory.remoteStorage()?.setOptions(FeatureA.B)
      awaitItem() shouldBe FeatureA.B
    }
  }
}
