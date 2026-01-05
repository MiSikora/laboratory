package io.mehow.laboratory.datastore

import androidx.datastore.core.DataStoreFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.Storage
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.addStorageSpec
import okio.ByteString.Companion.decodeHex

class DataStoreStorageSpec : FunSpec() {
  init {
    val tempFile = tempfile()
    val storage = Storage.dataStore(DataStoreFactory.create(StorageDataSerializer) { tempFile })

    afterTest { storage.clear() }

    addStorageSpec(storage)

    test("handle corrupted data") {
      val laboratory = Laboratory.create(storage)

      tempFile.writeBytes("00ff00ff".decodeHex().toByteArray())

      laboratory.experiment<FeatureA>() shouldBe FeatureA.A
    }
  }
}
