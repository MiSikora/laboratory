package io.mehow.laboratory.datastore

import androidx.datastore.core.DataStoreFactory
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.Storage
import io.mehow.laboratory.testing.AbstractStorageTest
import io.mehow.laboratory.testing.FeatureA
import java.io.File
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeHex
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreStorageTest : AbstractStorageTest() {
  @get:Rule val tempFolder = TemporaryFolder()

  private lateinit var tempFile: File

  @Before
  fun setUp() {
    tempFile = tempFolder.newFile("laboratory.pb")
  }

  override fun storage(): Storage {
    val dataStore = DataStoreFactory.create(StorageDataSerializer) { tempFile }
    return Storage.dataStore(dataStore)
  }

  @Test
  fun `handle corrupted data`() = runTest {
    val laboratory = Laboratory.create(storage())

    tempFile.writeBytes("00ff00ff".decodeHex().toByteArray())

    laboratory.experiment<FeatureA>() shouldBe FeatureA.A
  }
}
