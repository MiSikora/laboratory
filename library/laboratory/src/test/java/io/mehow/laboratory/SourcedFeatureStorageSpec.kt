package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

internal class SourcedFeatureStorageSpec : DescribeSpec({
  lateinit var localLaboratory: Laboratory
  lateinit var remoteLaboratoryA: Laboratory
  lateinit var remoteLaboratoryB: Laboratory
  lateinit var sourcedLaboratory: Laboratory

  beforeTest {
    val localStorage = FeatureStorage.inMemory()
    val remoteStorageA = FeatureStorage.inMemory()
    val remoteStorageB = FeatureStorage.inMemory()
    val sourcedStorage = SourcedFeatureStorage(
        localStorage,
        mapOf(
            "RemoteA" to remoteStorageA,
            "RemoteB" to remoteStorageB,
        ),
    )
    localLaboratory = Laboratory.create(localStorage)
    remoteLaboratoryA = Laboratory.create(remoteStorageA)
    remoteLaboratoryB = Laboratory.create(remoteStorageB)
    sourcedLaboratory = Laboratory.create(sourcedStorage)
  }

  describe("sourced feature storage") {
    context("with no sources set") {
      it("takes initial value from a default source") {
        localLaboratory.setOption(FirstFeature.B)
        sourcedLaboratory.experiment<FirstFeature>() shouldBe FirstFeature.B

        remoteLaboratoryB.setOption(SecondFeature.C)
        sourcedLaboratory.experiment<SecondFeature>() shouldBe SecondFeature.C
      }

      it("observes changes of a default feature source") {
        sourcedLaboratory.observe<SecondFeature>().test {
          expectItem() shouldBe SecondFeature.A

          remoteLaboratoryB.setOption(SecondFeature.B)
          expectItem() shouldBe SecondFeature.B

          remoteLaboratoryB.setOption(SecondFeature.C)
          expectItem() shouldBe SecondFeature.C

          cancel()
        }
      }
    }

    it("allows to change a feature source") {
      sourcedLaboratory.setOption(FirstFeature.Source.RemoteA)

      remoteLaboratoryA.setOption(FirstFeature.C)
      sourcedLaboratory.experiment<FirstFeature>() shouldBe FirstFeature.C
    }

    it("observes changes of a feature source") {
      localLaboratory.setOption(SecondFeature.A)
      remoteLaboratoryA.setOption(SecondFeature.B)
      remoteLaboratoryB.setOption(SecondFeature.C)

      sourcedLaboratory.observe<SecondFeature>().test {
        expectItem() shouldBe SecondFeature.C

        sourcedLaboratory.setOption(SecondFeature.Source.Local)
        expectItem() shouldBe SecondFeature.A

        localLaboratory.setOption(SecondFeature.C)
        expectItem() shouldBe SecondFeature.C

        sourcedLaboratory.setOption(SecondFeature.Source.RemoteA)
        expectItem() shouldBe SecondFeature.B

        remoteLaboratoryA.setOption(SecondFeature.A)
        expectItem() shouldBe SecondFeature.A

        sourcedLaboratory.setOption(SecondFeature.Source.RemoteB)
        expectItem() shouldBe SecondFeature.C

        remoteLaboratoryB.setOption(SecondFeature.B)
        expectItem() shouldBe SecondFeature.B

        cancel()
      }
    }

    it("does not observe changes of different feature source") {
      localLaboratory.setOption(SecondFeature.B)
      remoteLaboratoryA.setOption(SecondFeature.C)

      sourcedLaboratory.observe<SecondFeature>().test {
        expectItem() shouldBe SecondFeature.A

        sourcedLaboratory.setOption(FirstFeature.Source.Local)
        expectNoEvents()

        sourcedLaboratory.setOption(FirstFeature.Source.RemoteA)
        expectNoEvents()

        cancel()
      }
    }

    context("for empty source") {
      it("falls back to observing a local storage") {
        sourcedLaboratory.observe<EmptySourceFeature>().test {
          expectItem() shouldBe EmptySourceFeature.A

          localLaboratory.setOption(EmptySourceFeature.B)
          expectItem() shouldBe EmptySourceFeature.B

          cancel()
        }
      }

      it("falls backs to experimenting with a local storage") {
        sourcedLaboratory.experiment<EmptySourceFeature>() shouldBe EmptySourceFeature.A

        localLaboratory.setOption(EmptySourceFeature.C)
        sourcedLaboratory.experiment<EmptySourceFeature>() shouldBe EmptySourceFeature.C
      }
    }

    context("for unknown source") {
      it("falls back to observing a local storage") {
        val localStorage = FeatureStorage.inMemory()
        val sourcedStorage = SourcedFeatureStorage(localStorage, emptyMap())
        localLaboratory = Laboratory.create(localStorage)
        sourcedLaboratory = Laboratory.create(sourcedStorage)

        sourcedLaboratory.observe<FirstFeature>().test {
          expectItem() shouldBe FirstFeature.A

          localLaboratory.setOption(FirstFeature.B)
          expectItem() shouldBe FirstFeature.B

          cancel()
        }
      }

      it("falls backs to experimenting with a local storage") {
        val localStorage = FeatureStorage.inMemory()
        val sourcedStorage = SourcedFeatureStorage(localStorage, emptyMap())
        localLaboratory = Laboratory.create(localStorage)
        sourcedLaboratory = Laboratory.create(sourcedStorage)

        sourcedLaboratory.experiment<FirstFeature>() shouldBe FirstFeature.A

        localLaboratory.setOption(FirstFeature.C)
        sourcedLaboratory.experiment<FirstFeature>() shouldBe FirstFeature.C
      }
    }

    context("for unsourced featre") {
      it("falls back to observing a local storage") {
        sourcedLaboratory.observe<UnsourcedFeature>().test {
          expectItem() shouldBe UnsourcedFeature.A

          localLaboratory.setOption(UnsourcedFeature.B)
          expectItem() shouldBe UnsourcedFeature.B

          cancel()
        }
      }

      it("falls backs to experimenting with a local storage") {
        sourcedLaboratory.experiment<UnsourcedFeature>() shouldBe UnsourcedFeature.A

        localLaboratory.setOption(UnsourcedFeature.C)
        sourcedLaboratory.experiment<UnsourcedFeature>() shouldBe UnsourcedFeature.C
      }
    }

    it("controls local features") {
      sourcedLaboratory.observe<FirstFeature>().test {
        expectItem() shouldBe FirstFeature.A

        sourcedLaboratory.setOption(FirstFeature.B)
        expectItem() shouldBe FirstFeature.B

        sourcedLaboratory.setOption(FirstFeature.C)
        expectItem() shouldBe FirstFeature.C
      }
    }
  }
})

private enum class FirstFeature : Feature<FirstFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A

    @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  enum class Source : Feature<Source> {
    Local,
    RemoteA,
    ;

    override val defaultOption get() = Local
  }
}

private enum class SecondFeature : Feature<SecondFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A

    @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  enum class Source : Feature<Source> {
    Local,
    RemoteA,
    RemoteB,
    ;

    override val defaultOption get() = RemoteB
  }
}

private enum class EmptySourceFeature : Feature<EmptySourceFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A

  @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  private enum class Source : Feature<Source>
}

private enum class UnsourcedFeature : Feature<UnsourcedFeature> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A
}

