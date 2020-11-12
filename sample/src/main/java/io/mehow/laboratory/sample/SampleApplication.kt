package io.mehow.laboratory.sample

import android.app.Application
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.a.Authentication
import io.mehow.laboratory.b.PowerSource
import io.mehow.laboratory.c.DistanceAlgorithm
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.sharedpreferences.sharedPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class SampleApplication : Application() {
  private lateinit var localStorage: FeatureStorage
  private lateinit var firebaseStorage: FeatureStorage
  private lateinit var awsStorage: FeatureStorage
  private lateinit var azureStorage: FeatureStorage
  lateinit var laboratory: Laboratory
    private set

  override fun onCreate() {
    super.onCreate()
    localStorage = FeatureStorage.sharedPreferences(this, "localFeatures")
    firebaseStorage = FeatureStorage.sharedPreferences(this, "firebaseFeatures")
    awsStorage = FeatureStorage.sharedPreferences(this, "awsFeatures")
    azureStorage = FeatureStorage.sharedPreferences(this, "azureStorage")
    val sourcedStorage = FeatureStorage.sourcedGenerated(
        localSource = localStorage,
        firebaseSource = firebaseStorage,
        awsSource = awsStorage,
        azureSource = azureStorage,
    )
    laboratory = Laboratory.create(sourcedStorage)
    LaboratoryActivity.configure(
        laboratory = laboratory,
        mainFactory = FeatureFactory.featureGenerated(),
    )
    observeRemoteFeatures()
  }

  private fun observeRemoteFeatures() {
    firebaseStorage.observeRemoteFeature<DistanceAlgorithm>()
    azureStorage.observeRemoteFeature<DistanceAlgorithm>()
    firebaseStorage.observeRemoteFeature<PowerSource>()
    awsStorage.observeRemoteFeature<Authentication>()
  }

  private inline fun <reified T : Feature<T>> FeatureStorage.observeRemoteFeature() = GlobalScope.launch {
    val laboratory = Laboratory.create(this@observeRemoteFeature)
    val featureValues = T::class.java.enumConstants!!
    while (isActive) {
      delay(10_000)
      val nextFeatureValue = featureValues[Random.nextInt(featureValues.size)]
      laboratory.setOption(nextFeatureValue)
    }
  }

  companion object {
    fun getLaboratory(application: Application): Laboratory {
      return (application as SampleApplication).laboratory
    }
  }
}
