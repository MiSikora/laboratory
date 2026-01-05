package io.mehow.laboratory.sample.multimodule

import android.app.Application as AndroidApplication
import android.content.Context
import androidx.datastore.core.DataStoreFactory
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.Storage
import io.mehow.laboratory.datastore.StorageDataSerializer
import io.mehow.laboratory.datastore.dataStore
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.smaple.multimodule.c.generated as cameraFeatureGenerated
import java.io.File

class Application : AndroidApplication() {
  private lateinit var laboratory: Laboratory

  override fun onCreate() {
    super.onCreate()
    val dataStore =
      DataStoreFactory.create(StorageDataSerializer) { File(filesDir, "datastore/local") }
    val storage = Storage.dataStore(dataStore)
    laboratory = Laboratory.create(storage)
    LaboratoryActivity.configure(
      laboratory,
      mainFactory = FeatureFactory.generated(),
      externalFactories = mapOf("Camera" to FeatureFactory.cameraFeatureGenerated()),
    )
  }

  companion object {
    val Context.laboratory
      get() = (applicationContext as Application).laboratory
  }
}
