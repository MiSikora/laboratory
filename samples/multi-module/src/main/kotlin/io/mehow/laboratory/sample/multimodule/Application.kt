package io.mehow.laboratory.sample.multimodule

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.datastore.FeatureFlagsSerializer
import io.mehow.laboratory.datastore.dataStore
import io.mehow.laboratory.inspector.LaboratoryActivity
import java.io.File
import android.app.Application as AndroidApplication
import io.mehow.laboratory.smaple.multimodule.c.featureGenerated as cameraFeatureGenerated

class Application : AndroidApplication() {
  private lateinit var laboratory: Laboratory

  override fun onCreate() {
    super.onCreate()
    val dataStore = DataStoreFactory.create(FeatureFlagsSerializer) { File(filesDir, "datastore/local") }
    val storage = FeatureStorage.dataStore(dataStore)
    laboratory = Laboratory.create(storage)
    LaboratoryActivity.configure(
        laboratory,
        mainFactory = FeatureFactory.featureGenerated(),
        externalFactories = mapOf("Camera" to FeatureFactory.cameraFeatureGenerated())
    )
  }

  companion object {
    val Context.laboratory get() = (applicationContext as Application).laboratory
  }
}
