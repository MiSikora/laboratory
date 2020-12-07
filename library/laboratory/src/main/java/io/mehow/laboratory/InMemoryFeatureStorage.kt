package io.mehow.laboratory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class InMemoryFeatureStorage : FeatureStorage {
  private val featureFlow = MutableStateFlow(emptyMap<Class<*>, String>())

  override fun <T : Feature<*>> observeFeatureName(feature: Class<T>) = featureFlow
      .map { it[feature] }
      .distinctUntilChanged()

  override suspend fun <T : Feature<*>> getFeatureName(feature: Class<T>) = featureFlow.first()[feature]

  override suspend fun clear() = synchronized(this) {
    featureFlow.value = emptyMap()
    true
  }

  override suspend fun <T : Feature<*>> setOptions(vararg options: T) = synchronized(this) {
    val features = featureFlow.value
    val newFeatures = options.associate { it.javaClass to it.name }
    featureFlow.value = features + newFeatures
    true
  }
}
