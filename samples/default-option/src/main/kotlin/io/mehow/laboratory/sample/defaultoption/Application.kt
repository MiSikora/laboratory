package io.mehow.laboratory.sample.defaultoption

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import io.mehow.laboratory.DefaultOptionFactory
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.datastore.FeatureFlagsSerializer
import io.mehow.laboratory.datastore.dataStore
import io.mehow.laboratory.inspector.LaboratoryActivity
import java.io.File
import android.app.Application as AndroidApplication

class Application : AndroidApplication() {
  private lateinit var laboratory: Laboratory

  override fun onCreate() {
    super.onCreate()
    val dataStore = DataStoreFactory.create(FeatureFlagsSerializer) { File(filesDir, "datastore/local") }
    val storage = FeatureStorage.dataStore(dataStore)
    laboratory = Laboratory.builder()
        .featureStorage(storage)
        .defaultOptionFactory(DefaultOptionFactory.create())
        .build()
    LaboratoryActivity.configure(laboratory, FeatureFactory.featureGenerated())
  }

  companion object {
    val Context.laboratory get() = (applicationContext as Application).laboratory
  }
}
