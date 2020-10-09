package io.mehow.laboratory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class Laboratory(private val storage: FeatureStorage) {
  inline fun <reified T : Feature<T>> observe() = observe(T::class.java)

  fun <T : Feature<T>> observe(featureClass: Class<T>): Flow<T> {
    val (features, defaultFeature) = extractFeatureMetadata(featureClass)
    return storage.observeFeatureName(featureClass).map { featureName ->
      val expectedName = featureName ?: defaultFeature.name
      features.firstOrNull { it.name == expectedName } ?: defaultFeature
    }
  }

  suspend inline fun <reified T : Feature<T>> experiment() = experiment(T::class.java)

  @BlockingIoCall
  inline fun <reified T : Feature<T>> experimentBlocking() = runBlocking { experiment<T>() }

  suspend fun <T : Feature<T>> experiment(featureClass: Class<T>): T {
    val (features, defaultFeature) = extractFeatureMetadata(featureClass)
    val expectedName = storage.getFeatureName(defaultFeature.javaClass) ?: defaultFeature.name
    return features.firstOrNull { it.name == expectedName } ?: defaultFeature
  }

  @BlockingIoCall
  fun <T : Feature<T>> experimentBlocking(featureClass: Class<T>) = runBlocking { experiment(featureClass) }

  suspend fun <T : Feature<*>> setFeature(feature: T) = storage.setFeature(feature)

  @BlockingIoCall
  fun <T : Feature<*>> setFeatureBlocking(feature: T) = runBlocking { storage.setFeature(feature) }

  suspend fun <T : Feature<*>> setFeatures(vararg features: T) = storage.setFeatures(*features)

  @BlockingIoCall
  fun <T : Feature<*>> setFeaturesBlocking(vararg features: T) = runBlocking { setFeatures(*features) }

  private fun <T : Feature<T>> extractFeatureMetadata(group: Class<T>): Pair<Array<T>, T> {
    val features = requireNotNull(group.enumConstants) { "${group.name} must be an enum" }
    require(features.isNotEmpty()) { "${group.name} must have at least one value" }
    val defaultFeature = features.firstOrNull { it.isDefaultValue } ?: features.first()
    return features to defaultFeature
  }

  companion object {
    fun inMemory() = Laboratory(FeatureStorage.inMemory())
  }
}
