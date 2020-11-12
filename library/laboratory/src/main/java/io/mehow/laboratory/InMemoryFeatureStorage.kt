package io.mehow.laboratory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class InMemoryFeatureStorage : FeatureStorage {
  private var features = emptyMap<Class<*>, String>()
  private val featureFlow = MutableStateFlow(features)

  override fun <T : Feature<*>> observeFeatureName(feature: Class<T>) = featureFlow
      .map { it[feature] }
      .distinctUntilChanged()

  override suspend fun <T : Feature<*>> getFeatureName(feature: Class<T>) = features[feature]

  override suspend fun clear(): Boolean {
    features = emptyMap()
    featureFlow.value = emptyMap()
    return true
  }

  override suspend fun <T : Feature<*>> setOptions(vararg options: T): Boolean {
    for (feature in options) {
      this.features += feature.javaClass to feature.name
    }
    featureFlow.value = this.features
    return true
  }
}
