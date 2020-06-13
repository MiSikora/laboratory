package io.mehow.laboratory.sample

import android.app.Application
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.android.SharedPreferencesFeatureStorage
import io.mehow.laboratory.inspector.LaboratoryActivity

class SampleApplication : Application() {
  lateinit var laboratory: Laboratory
    private set

  override fun onCreate() {
    super.onCreate()
    val sharedPreferences = getSharedPreferences("laboratory", MODE_PRIVATE)
    val featureStorage = SharedPreferencesFeatureStorage(sharedPreferences)
    laboratory = Laboratory(featureStorage)
    LaboratoryActivity.initialize(FeatureFactory.generated(), featureStorage)
  }

  companion object {
    fun getLaboratory(application: Application): Laboratory {
      return (application as SampleApplication).laboratory
    }
  }
}
