package io.mehow.laboratory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class InMemoryFeatureStorage : FeatureStorage {
  private val featureFlow = MutableStateFlow(emptyMap<Class<*>, String>())

  override fun <T : Feature<*>> observeFeatureName(feature: Class<T>) = featureFlow
      .map { it[feature] }
      .distinctUntilChanged()

  override suspend fun <T : Feature<*>> getFeatureName(feature: Class<T>) = featureFlow.map { it[feature] }.first()

  override suspend fun clear(): Boolean {
    featureFlow.update { emptyMap() }
    return true
  }

  override suspend fun <T : Feature<*>> setOptions(vararg options: T): Boolean {
    val newFeatures = options.associate { it.javaClass to it.name }
    featureFlow.update { it + newFeatures }
    return true
  }
}
