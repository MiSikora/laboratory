package io.mehow.laboratory.sample

import android.app.Application
import io.mehow.laboratory.DefaultOptionFactory
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.a.AllowScreenshots
import io.mehow.laboratory.a.Authentication
import io.mehow.laboratory.b.PowerSource
import io.mehow.laboratory.c.DistanceAlgorithm
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.options
import io.mehow.laboratory.sharedpreferences.sharedPreferences
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import io.mehow.laboratory.christmas.featureGenerated as christmasGenerated

class SampleApplication : Application() {
  private lateinit var localStorage: FeatureStorage
  private lateinit var firebaseStorage: FeatureStorage
  private lateinit var awsStorage: FeatureStorage
  private lateinit var azureStorage: FeatureStorage
  lateinit var laboratory: Laboratory
    private set

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun onCreate() {
    super.onCreate()
    localStorage = FeatureStorage.sharedPreferences(getSharedPreferences("localFeatures", MODE_PRIVATE))
    firebaseStorage = FeatureStorage.sharedPreferences(getSharedPreferences("firebaseFeatures", MODE_PRIVATE))
    awsStorage = FeatureStorage.sharedPreferences(getSharedPreferences("awsFeatures", MODE_PRIVATE))
    azureStorage = FeatureStorage.sharedPreferences(getSharedPreferences("azureStorage", MODE_PRIVATE))
    val sourcedStorage = FeatureStorage.sourcedBuilder(localStorage)
        .awsSource(awsStorage)
        .azureSource(azureStorage)
        .firebaseSource(firebaseStorage)
        .build()
    laboratory = Laboratory.builder()
        .featureStorage(sourcedStorage)
        .defaultOptionFactory(SampleDefaultOptionFactory)
        .build()
    LaboratoryActivity.configure(
        laboratory = laboratory,
        mainFactory = FeatureFactory.featureGenerated(),
        externalFactories = mapOf("Christmas" to FeatureFactory.christmasGenerated()),
    )
    observeRemoteFeatures()
  }

  private fun observeRemoteFeatures() {
    firebaseStorage.observeRemoteFeature<DistanceAlgorithm>()
    azureStorage.observeRemoteFeature<DistanceAlgorithm>()
    firebaseStorage.observeRemoteFeature<PowerSource>()
    awsStorage.observeRemoteFeature<Authentication>()
  }

  @OptIn(DelicateCoroutinesApi::class)
  private inline fun <reified T : Feature<T>> FeatureStorage.observeRemoteFeature() = GlobalScope.launch {
    val laboratory = Laboratory.create(this@observeRemoteFeature)
    val featureValues = T::class.java.options
    while (isActive) {
      delay(10_000)
      val nextFeatureValue = featureValues[Random.nextInt(featureValues.size)]
      laboratory.setOption(nextFeatureValue)
    }
  }

  private object SampleDefaultOptionFactory : DefaultOptionFactory {
    override fun <T : Feature<out T>> create(feature: T): Feature<*>? = when (feature) {
      is DistanceAlgorithm -> DistanceAlgorithm.Cosine
      is AllowScreenshots -> AllowScreenshots.Enabled
      else -> null
    }
  }

  companion object {
    fun getLaboratory(application: Application): Laboratory {
      return (application as SampleApplication).laboratory
    }
  }
}
