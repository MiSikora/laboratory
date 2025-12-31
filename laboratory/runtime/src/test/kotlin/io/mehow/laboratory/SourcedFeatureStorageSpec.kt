package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SourcedFeatureStorageSpec : FunSpec() {
  enum class FeatureA : Feature<FeatureA> {
    A,
    B,
    C;

    override val defaultOption
      get() = A

    override val source = Source::class.java

    enum class Source : Feature<Source> {
      Local,
      RemoteA;

      override val defaultOption
        get() = Local
    }
  }

  enum class FeatureB : Feature<FeatureB> {
    A,
    B,
    C;

    override val defaultOption
      get() = A

    override val source = Source::class.java

    enum class Source : Feature<Source> {
      Local,
      RemoteA,
      RemoteB;

      override val defaultOption
        get() = RemoteB
    }
  }

  enum class EmptySourceFeature : Feature<EmptySourceFeature> {
    A,
    B,
    C;

    override val defaultOption
      get() = A

    override val source = Source::class.java

    enum class Source : Feature<Source>
  }

  enum class UnsourcedFeature : Feature<UnsourcedFeature> {
    A,
    B,
    C;

    override val defaultOption
      get() = A
  }

  init {
    val storageLocal = FeatureStorage.inMemory()
    val storageRemoteA = FeatureStorage.inMemory()
    val storageRemoteB = FeatureStorage.inMemory()
    val storageSourced =
      SourcedFeatureStorage(
        storageLocal,
        mapOf("RemoteA" to storageRemoteA, "RemoteB" to storageRemoteB),
      )

    val laboratoryLocal = Laboratory.create(storageLocal)
    val laboratoryRemoteA = Laboratory.create(storageRemoteA)
    val laboratoryRemoteB = Laboratory.create(storageRemoteB)
    val laboratorySourced = Laboratory.create(storageSourced)

    beforeTest {
      laboratoryLocal.clear()
      laboratoryRemoteA.clear()
      laboratoryRemoteB.clear()
    }

    test("uses options from default sources") {
      laboratoryLocal.setOption(FeatureA.B)
      laboratorySourced.experiment<FeatureA>() shouldBe FeatureA.B

      laboratoryRemoteB.setOption(FeatureB.C)
      laboratorySourced.experiment<FeatureB>() shouldBe FeatureB.C
    }

    test("emits options from default sources") {
      laboratorySourced.observe<FeatureB>().test {
        awaitItem() shouldBe FeatureB.A

        laboratoryRemoteB.setOption(FeatureB.B)
        awaitItem() shouldBe FeatureB.B

        laboratoryRemoteB.setOption(FeatureB.C)
        awaitItem() shouldBe FeatureB.C
      }
    }

    test("uses options from changed sources") {
      laboratorySourced.setOption(FeatureA.Source.RemoteA)

      laboratoryRemoteA.setOption(FeatureA.C)
      laboratorySourced.experiment<FeatureA>() shouldBe FeatureA.C

      laboratoryLocal.setOption(FeatureA.B)
      laboratorySourced.experiment<FeatureA>() shouldBe FeatureA.C
    }

    test("emits options from changed sources") {
      laboratoryLocal.setOption(FeatureB.A)
      laboratoryRemoteA.setOption(FeatureB.B)
      laboratoryRemoteB.setOption(FeatureB.C)

      laboratorySourced.observe<FeatureB>().test {
        awaitItem() shouldBe FeatureB.C

        laboratorySourced.setOption(FeatureB.Source.Local)
        awaitItem() shouldBe FeatureB.A

        laboratoryLocal.setOption(FeatureB.C)
        awaitItem() shouldBe FeatureB.C

        laboratorySourced.setOption(FeatureB.Source.RemoteA)
        awaitItem() shouldBe FeatureB.B

        laboratoryRemoteA.setOption(FeatureB.A)
        awaitItem() shouldBe FeatureB.A

        laboratorySourced.setOption(FeatureB.Source.RemoteB)
        awaitItem() shouldBe FeatureB.C

        laboratoryRemoteB.setOption(FeatureB.B)
        awaitItem() shouldBe FeatureB.B
      }
    }

    test("does not emit changes from inactive sources") {
      laboratorySourced.observe<FeatureB>().test {
        awaitItem() shouldBe FeatureB.A

        laboratoryLocal.setOption(FeatureB.B)
        expectNoEvents()

        laboratoryRemoteA.setOption(FeatureB.B)
        expectNoEvents()
      }
    }

    test("does not mix source changes between different features") {
      laboratoryLocal.setOption(FeatureB.B)
      laboratoryRemoteA.setOption(FeatureB.C)

      laboratorySourced.observe<FeatureB>().test {
        awaitItem() shouldBe FeatureB.A

        laboratorySourced.setOption(FeatureA.Source.Local)
        expectNoEvents()

        laboratorySourced.setOption(FeatureA.Source.RemoteA)
        expectNoEvents()
      }
    }

    test("clears only local storage") {
      laboratoryLocal.setOption(FeatureA.B)
      laboratoryRemoteA.setOption(FeatureA.B)
      laboratorySourced.setOption(FeatureA.Source.RemoteA)

      laboratorySourced.clear()

      laboratoryLocal.experimentIs(FeatureA.A)
      laboratoryRemoteA.experimentIs(FeatureA.B)
      laboratorySourced.experimentIs(FeatureA.A)
    }

    test("allows to override default sources") {
      val defaultOptionFactory =
        object : DefaultOptionFactory {
          override fun <T : Feature<out T>> create(feature: T) =
            when (feature) {
              is FeatureA.Source -> FeatureA.Source.RemoteA
              else -> null
            }
        }
      val laboratory =
        Laboratory.Builder()
          .featureStorage(storageSourced)
          .defaultOptionFactory(defaultOptionFactory)
          .build()

      laboratoryRemoteA.setOption(FeatureA.C)

      laboratory.experiment<FeatureA>() shouldBe FeatureA.C
    }

    test("makes changes only in local storage") {
      laboratoryLocal.setOption(FeatureA.A)
      laboratoryRemoteA.setOption(FeatureA.A)
      laboratoryRemoteB.setOption(FeatureA.A)

      laboratorySourced.setOption(FeatureA.B)

      laboratoryLocal.experiment<FeatureA>() shouldBe FeatureA.B
      laboratoryRemoteA.experiment<FeatureA>() shouldBe FeatureA.A
      laboratoryRemoteB.experiment<FeatureA>() shouldBe FeatureA.A
    }

    context("feature with empty sources") {
      test("reads from a local storage") {
        laboratorySourced.experiment<EmptySourceFeature>() shouldBe EmptySourceFeature.A

        laboratoryLocal.setOption(EmptySourceFeature.C)
        laboratorySourced.experiment<EmptySourceFeature>() shouldBe EmptySourceFeature.C
      }

      test("emits changes from a local storage") {
        laboratorySourced.observe<EmptySourceFeature>().test {
          awaitItem() shouldBe EmptySourceFeature.A

          laboratoryLocal.setOption(EmptySourceFeature.B)
          awaitItem() shouldBe EmptySourceFeature.B

          cancel()
        }
      }
    }

    context("feature with no sources") {
      test("reads from a local storage") {
        laboratorySourced.experiment<UnsourcedFeature>() shouldBe UnsourcedFeature.A

        laboratoryLocal.setOption(UnsourcedFeature.C)
        laboratorySourced.experiment<UnsourcedFeature>() shouldBe UnsourcedFeature.C
      }

      test("emits changes from a local storage") {
        laboratorySourced.observe<UnsourcedFeature>().test {
          awaitItem() shouldBe UnsourcedFeature.A

          laboratoryLocal.setOption(UnsourcedFeature.B)
          awaitItem() shouldBe UnsourcedFeature.B

          cancel()
        }
      }
    }

    context("feature with unknown source") {
      val localStorage = FeatureStorage.inMemory()
      val sourcedStorage = SourcedFeatureStorage(localStorage, emptyMap())
      val laboratoryLocal = Laboratory.create(localStorage)
      val laboratorySourced = Laboratory.create(sourcedStorage)

      beforeTest { laboratoryLocal.clear() }

      test("reads from a local storage") {
        laboratorySourced.experiment<FeatureA>() shouldBe FeatureA.A

        laboratoryLocal.setOption(FeatureA.C)
        laboratorySourced.experiment<FeatureA>() shouldBe FeatureA.C
      }

      test("emits changes from a local storage") {
        laboratorySourced.observe<FeatureA>().test {
          awaitItem() shouldBe FeatureA.A

          laboratoryLocal.setOption(FeatureA.B)
          awaitItem() shouldBe FeatureA.B

          cancel()
        }
      }
    }
  }
}
