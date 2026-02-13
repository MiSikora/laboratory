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
      val localStore =
          DataStoreFactory.create(StorageDataSerializer) { File(filesDir, "datastore/local") }
      val remoteStore =
          DataStoreFactory.create(StorageDataSerializer) { File(filesDir, "datastore/remote") }
      val localStorage = Storage.dataStore(localStore)
      val remoteStorage = Storage.dataStore(remoteStore)
    laboratory = Laboratory
        .builder()
        .localStorage(localStorage)
        .remoteStorage(remoteStorage)
        .build()
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
