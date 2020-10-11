package io.mehow.laboratory.sample

import android.app.Application
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.sharedpreferences.SharedPreferencesFeatureStorage

class SampleApplication : Application() {
  lateinit var laboratory: Laboratory
    private set

  @Suppress("LongMethod")
  override fun onCreate() {
    super.onCreate()
    val localStorage = SharedPreferencesFeatureStorage(
      getSharedPreferences("localFeatures", MODE_PRIVATE)
    )
    val firebaseStorage = SharedPreferencesFeatureStorage(
      getSharedPreferences("firebaseFeatures", MODE_PRIVATE)
    )
    val awsStorage = SharedPreferencesFeatureStorage(
      getSharedPreferences("awsFeatures", MODE_PRIVATE)
    )
    val sourcedStorage = FeatureStorage.sourcedGenerated(
      localSource = localStorage,
      firebaseSource = firebaseStorage,
      awsSource = awsStorage,
    )
    laboratory = Laboratory(sourcedStorage)
    LaboratoryActivity.configure(
      localStorage = localStorage,
      featureFactory = FeatureFactory.featureGenerated(),
//      featureSourceFactory = FeatureFactory.featureSourceGenerated()
    )
  }

  companion object {
    fun getLaboratory(application: Application): Laboratory {
      return (application as SampleApplication).laboratory
    }
  }
}
