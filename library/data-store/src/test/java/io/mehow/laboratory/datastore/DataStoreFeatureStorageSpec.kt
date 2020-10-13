package io.mehow.laboratory.datastore

import app.cash.turbine.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import okio.ByteString.Companion.decodeHex

class DataStoreFeatureStorageSpec : StringSpec({
  "stored feature is available as experiment" {
    val tempFile = tempfile()
    val storage = FeatureStorage.dataStore { tempFile }
    val laboratory = Laboratory(storage)

    storage.setFeature(FeatureA.B)

    laboratory.experiment<FeatureA>() shouldBe FeatureA.B
  }

  "corrupted file yields default experiment" {
    val tempFile = tempfile()
    val storage = FeatureStorage.dataStore { tempFile }
    val laboratory = Laboratory(storage)

    // Represents a map<string, int> with a key of Feature::class.java.name and value of 1.
    val corruptedBytes = "0a290a25696f2e6d65686f772e6c61626f7261746f72792e6461746173746f72652e466561747572651001"
      .decodeHex()
      .toByteArray()
    tempFile.writeBytes(corruptedBytes)

    laboratory.experiment<FeatureA>() shouldBe FeatureA.A
  }

  "observes feature changes" {
    val tempFile = tempfile()
    val storage = FeatureStorage.dataStore { tempFile }

    storage.observeFeatureName(FeatureA::class.java).test {
      expectItem() shouldBe null

      storage.setFeature(FeatureA.B)
      expectItem() shouldBe FeatureA.B.name

      storage.setFeature(FeatureA.B)
      expectNoEvents()

      storage.setFeature(FeatureA.A)
      expectItem() shouldBe FeatureA.A.name

      cancel()
    }
  }
})

private enum class FeatureA(override val isDefaultValue: Boolean = false) : Feature<FeatureA> {
  A,
  B,
  ;
}
