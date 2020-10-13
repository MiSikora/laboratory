package io.mehow.laboratory.sample

import android.app.Application
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.sharedpreferences.sharedPreferences

class SampleApplication : Application() {
  lateinit var laboratory: Laboratory
    private set

  override fun onCreate() {
    super.onCreate()
    val sourcedStorage = FeatureStorage.sourcedGenerated(
      localSource = FeatureStorage.sharedPreferences(this, "localFeatures"),
      firebaseSource = FeatureStorage.sharedPreferences(this, "firebaseFeatures"),
      awsSource = FeatureStorage.sharedPreferences(this, "awsFeatures"),
      azureSource = FeatureStorage.sharedPreferences(this, "azureStorage"),
    )
    laboratory = Laboratory(sourcedStorage)
    LaboratoryActivity.configure(
      laboratory = laboratory,
      featureFactory = FeatureFactory.featureGenerated(),
    )
  }

  companion object {
    fun getLaboratory(application: Application): Laboratory {
      return (application as SampleApplication).laboratory
    }
  }
}
