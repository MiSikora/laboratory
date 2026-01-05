package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import io.mehow.laboratory.testing.FeatureWithoutValues
import io.mehow.laboratory.testing.RemoteFeatureA
import io.mehow.laboratory.testing.RemoteFeatureB

class LaboratorySpec : FunSpec() {
  init {
    val laboratory =
      Laboratory.builder()
        .localStorage(Storage.inMemory())
        .remoteStorage(Storage.inMemory())
        .build()

    beforeTest {
      laboratory.localStorage().clear()
      laboratory.remoteStorage()?.clear()
    }

    test("change feature option") {
      for (option in FeatureA::class.java.options) {
        laboratory.localStorage().setOptions(option)

        laboratory.experiment<FeatureA>() shouldBe option
      }
    }

    test("change multiple feature options") {
      laboratory.localStorage().setOptions(FeatureA.C, FeatureB.B)

      laboratory.experiment<FeatureA>() shouldBe FeatureA.C
      laboratory.experiment<FeatureB>() shouldBe FeatureB.B
    }

    test("fail to use feature with no values") {
      val exception =
        shouldThrow<IllegalStateException> { laboratory.experiment<FeatureWithoutValues>() }
      exception shouldHaveMessage
        "io.mehow.laboratory.testing.FeatureWithoutValues must have at least one option"
    }

    test("read local feature") {
      laboratory.experiment<FeatureA>() shouldBe FeatureA.A

      laboratory.localStorage().setOptions(FeatureA.B)
      laboratory.experiment<FeatureA>() shouldBe FeatureA.B
    }

    test("read remote feature") {
      laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.A

      laboratory.remoteStorage()?.setOptions(RemoteFeatureA.B)
      laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.B
    }

    test("change feature to remote") {
      laboratory.remoteStorage()?.setOptions(FeatureA.C)
      laboratory.experiment<FeatureA>() shouldBe FeatureA.A

      laboratory.localStorage().setRemoteSource<FeatureA>()
      laboratory.experiment<FeatureA>() shouldBe FeatureA.C
    }

    test("change feature to local") {
      laboratory.localStorage().setOptions(RemoteFeatureA.C)
      laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.A

      laboratory.localStorage().setLocalSource<RemoteFeatureA>()
      laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.C
    }

    test("clear local features") {
      laboratory.localStorage().setOptions(FeatureA.C, FeatureB.C)
      laboratory.localStorage().clear()

      laboratory.experiment<FeatureA>() shouldBe FeatureA.A
      laboratory.experiment<FeatureB>() shouldBe FeatureB.B
    }

    test("clear remote features") {
      laboratory.remoteStorage()?.setOptions(RemoteFeatureA.C, RemoteFeatureB.C)
      laboratory.remoteStorage()?.clear()

      laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.A
      laboratory.experiment<RemoteFeatureB>() shouldBe RemoteFeatureB.B
    }

    test("observe feature") {
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

    context("default option factory") {
      val factory =
        object : DefaultOptionFactory {
          override fun <T : Feature<out T>> create(feature: T) =
            when (feature) {
              is FeatureA -> FeatureA.C
              is FeatureB -> FeatureA.C // Intentionally wrong class
              else -> null
            }
        }

      val laboratory =
        Laboratory.builder().localStorage(Storage.inMemory()).defaultOptionFactory(factory).build()

      beforeTest { laboratory.localStorage().clear() }

      test("override default option") { laboratory.experiment<FeatureA>() shouldBe FeatureA.C }

      test("do not override changed option") {
        for (option in FeatureA::class.java.options) {
          laboratory.localStorage().setOptions(option)

          laboratory.experiment<FeatureA>() shouldBe option
        }
      }

      test("override default option in flow") {
        laboratory.observe<FeatureA>().test {
          awaitItem() shouldBe FeatureA.C

          laboratory.localStorage().setOptions(FeatureA.B)
          awaitItem() shouldBe FeatureA.B
        }
      }

      test("fail when provided default option uses wrong type") {
        val exception = shouldThrow<IllegalStateException> { laboratory.experiment<FeatureB>() }
        exception shouldHaveMessage
          "Tried to use FeatureA.C as a default option for io.mehow.laboratory.testing.FeatureB"
      }
    }

    context("default source factory") {
      val factory =
        object : DefaultSourceFactory {
          override fun <T : Feature<out T>> create(feature: T) =
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

      beforeTest {
        laboratory.localStorage().clear()
        laboratory.remoteStorage()?.clear()
      }

      test("override default local source") {
        laboratory.remoteStorage()?.setOptions(FeatureA.C)

        laboratory.experiment<FeatureA>() shouldBe FeatureA.C
      }

      test("override default remote source") {
        laboratory.localStorage().setOptions(RemoteFeatureA.C)

        laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.C
      }

      test("do not override changed local source") {
        laboratory.localStorage().setLocalSource<FeatureA>()
        laboratory.remoteStorage()?.setOptions(FeatureA.C)

        laboratory.experiment<FeatureA>() shouldBe FeatureA.A
      }

      test("do not override changed remote source") {
        laboratory.localStorage().setRemoteSource<RemoteFeatureA>()
        laboratory.localStorage().setOptions(RemoteFeatureA.C)

        laboratory.experiment<RemoteFeatureA>() shouldBe RemoteFeatureA.A
      }

      test("override default source in flow") {
        laboratory.observe<FeatureA>().test {
          awaitItem() shouldBe FeatureA.A

          laboratory.remoteStorage()?.setOptions(FeatureA.B)
          awaitItem() shouldBe FeatureA.B
        }
      }
    }
  }
}
