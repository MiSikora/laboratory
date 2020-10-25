package io.mehow.laboratory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class InMemoryFeatureStorage : FeatureStorage {
  private var features = emptyMap<Class<*>, String>()
  private val featureFlow = MutableStateFlow(features)

  override fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>) = featureFlow
      .map { it[featureClass] }
      .distinctUntilChanged()

  override suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>) = features[featureClass]

  override suspend fun <T : Feature<*>> setFeatures(vararg values: T): Boolean {
    for (feature in values) {
      this.features += feature.javaClass to feature.name
    }
    featureFlow.value = this.features
    return true
  }
}
