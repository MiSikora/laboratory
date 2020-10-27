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
    localLaboratory = Laboratory(localStorage)
    remoteLaboratoryA = Laboratory(remoteStorageA)
    remoteLaboratoryB = Laboratory(remoteStorageB)
    sourcedLaboratory = Laboratory(sourcedStorage)
  }

  describe("sourced feature storage") {
    context("with no sources set") {
      it("takes initial value from a default source") {
        localLaboratory.setFeature(FirstFeature.B)
        sourcedLaboratory.experiment<FirstFeature>() shouldBe FirstFeature.B

        remoteLaboratoryB.setFeature(SecondFeature.C)
        sourcedLaboratory.experiment<SecondFeature>() shouldBe SecondFeature.C
      }

      it("observes changes of a default feature source") {
        sourcedLaboratory.observe<SecondFeature>().test {
          expectItem() shouldBe SecondFeature.A

          remoteLaboratoryB.setFeature(SecondFeature.B)
          expectItem() shouldBe SecondFeature.B

          remoteLaboratoryB.setFeature(SecondFeature.C)
          expectItem() shouldBe SecondFeature.C

          cancel()
        }
      }
    }

    it("allows to change a feature source") {
      sourcedLaboratory.setFeature(FirstFeature.Source.RemoteA)

      remoteLaboratoryA.setFeature(FirstFeature.C)
      sourcedLaboratory.experiment<FirstFeature>() shouldBe FirstFeature.C
    }

    it("observes changes of a feature source") {
      localLaboratory.setFeature(SecondFeature.A)
      remoteLaboratoryA.setFeature(SecondFeature.B)
      remoteLaboratoryB.setFeature(SecondFeature.C)

      sourcedLaboratory.observe<SecondFeature>().test {
        expectItem() shouldBe SecondFeature.C

        sourcedLaboratory.setFeature(SecondFeature.Source.Local)
        expectItem() shouldBe SecondFeature.A

        localLaboratory.setFeature(SecondFeature.C)
        expectItem() shouldBe SecondFeature.C

        sourcedLaboratory.setFeature(SecondFeature.Source.RemoteA)
        expectItem() shouldBe SecondFeature.B

        remoteLaboratoryA.setFeature(SecondFeature.A)
        expectItem() shouldBe SecondFeature.A

        sourcedLaboratory.setFeature(SecondFeature.Source.RemoteB)
        expectItem() shouldBe SecondFeature.C

        remoteLaboratoryB.setFeature(SecondFeature.B)
        expectItem() shouldBe SecondFeature.B

        cancel()
      }
    }

    it("does not observe changes of different feature source") {
      localLaboratory.setFeature(SecondFeature.B)
      remoteLaboratoryA.setFeature(SecondFeature.C)

      sourcedLaboratory.observe<SecondFeature>().test {
        expectItem() shouldBe SecondFeature.A

        sourcedLaboratory.setFeature(FirstFeature.Source.Local)
        expectNoEvents()

        sourcedLaboratory.setFeature(FirstFeature.Source.RemoteA)
        expectNoEvents()

        cancel()
      }
    }

    context("for empty source") {
      it("falls back to observing a local storage") {
        sourcedLaboratory.observe<EmptySourceFeature>().test {
          expectItem() shouldBe EmptySourceFeature.A

          localLaboratory.setFeature(EmptySourceFeature.B)
          expectItem() shouldBe EmptySourceFeature.B

          cancel()
        }
      }

      it("falls backs to experimenting with a local storage") {
        sourcedLaboratory.experiment<EmptySourceFeature>() shouldBe EmptySourceFeature.A

        localLaboratory.setFeature(EmptySourceFeature.C)
        sourcedLaboratory.experiment<EmptySourceFeature>() shouldBe EmptySourceFeature.C
      }
    }

    context("for unknown source") {
      it("falls back to observing a local storage") {
        val localStorage = FeatureStorage.inMemory()
        val sourcedStorage = SourcedFeatureStorage(localStorage, emptyMap())
        localLaboratory = Laboratory(localStorage)
        sourcedLaboratory = Laboratory(sourcedStorage)

        sourcedLaboratory.observe<FirstFeature>().test {
          expectItem() shouldBe FirstFeature.A

          localLaboratory.setFeature(FirstFeature.B)
          expectItem() shouldBe FirstFeature.B

          cancel()
        }
      }

      it("falls backs to experimenting with a local storage") {
        val localStorage = FeatureStorage.inMemory()
        val sourcedStorage = SourcedFeatureStorage(localStorage, emptyMap())
        localLaboratory = Laboratory(localStorage)
        sourcedLaboratory = Laboratory(sourcedStorage)

        sourcedLaboratory.experiment<FirstFeature>() shouldBe FirstFeature.A

        localLaboratory.setFeature(FirstFeature.C)
        sourcedLaboratory.experiment<FirstFeature>() shouldBe FirstFeature.C
      }
    }

    context("for unsourced featre") {
      it("falls back to observing a local storage") {
        sourcedLaboratory.observe<UnsourcedFeature>().test {
          expectItem() shouldBe UnsourcedFeature.A

          localLaboratory.setFeature(UnsourcedFeature.B)
          expectItem() shouldBe UnsourcedFeature.B

          cancel()
        }
      }

      it("falls backs to experimenting with a local storage") {
        sourcedLaboratory.experiment<UnsourcedFeature>() shouldBe UnsourcedFeature.A

        localLaboratory.setFeature(UnsourcedFeature.C)
        sourcedLaboratory.experiment<UnsourcedFeature>() shouldBe UnsourcedFeature.C
      }
    }

    it("controls local features") {
      sourcedLaboratory.observe<FirstFeature>().test {
        expectItem() shouldBe FirstFeature.A

        sourcedLaboratory.setFeature(FirstFeature.B)
        expectItem() shouldBe FirstFeature.B

        sourcedLaboratory.setFeature(FirstFeature.C)
        expectItem() shouldBe FirstFeature.C
      }
    }
  }
})

private enum class FirstFeature(override val isDefaultValue: Boolean = false) : Feature<FirstFeature> {
  A,
  B,
  C,
  ;

  @Suppress("UNCHECKED_CAST")
  override val sourcedWith = Source::class.java as Class<Feature<*>>

  enum class Source(override val isDefaultValue: Boolean = false) : Feature<Source> {
    Local,
    RemoteA,
    ;
  }
}

private enum class SecondFeature(override val isDefaultValue: Boolean = false) : Feature<SecondFeature> {
  A,
  B,
  C,
  ;

  @Suppress("UNCHECKED_CAST")
  override val sourcedWith = Source::class.java as Class<Feature<*>>

  enum class Source(override val isDefaultValue: Boolean = false) : Feature<Source> {
    Local,
    RemoteA,
    RemoteB(isDefaultValue = true),
    ;
  }
}

private enum class EmptySourceFeature(override val isDefaultValue: Boolean = false) : Feature<EmptySourceFeature> {
  A,
  B,
  C,
  ;

  @Suppress("UNCHECKED_CAST")
  override val sourcedWith = Source::class.java as Class<Feature<*>>

  private enum class Source : Feature<Source>
}

private enum class UnsourcedFeature(override val isDefaultValue: Boolean = false) : Feature<UnsourcedFeature> {
  A,
  B,
  C,
  ;
}

