package io.mehow.laboratory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class InMemoryFeatureStorage : FeatureStorage {
  private val featureFlow = MutableStateFlow(emptyMap<Class<out Feature<*>>, String>())

  override fun observeFeatureName(feature: Class<out Feature<*>>) = featureFlow
      .map { it[feature] }
      .distinctUntilChanged()

  override suspend fun getFeatureName(feature: Class<out Feature<*>>) = featureFlow.map { it[feature] }.first()

  override suspend fun clear(): Boolean {
    featureFlow.update { emptyMap() }
    return true
  }

  override suspend fun setOptions(
    vararg options: Feature<*>,
  ) = setOptions(options.associate { it.javaClass to it.name })

  override suspend fun setOptions(
    options: Collection<Feature<*>>,
  ) = setOptions(options.associate { it.javaClass to it.name })

  private fun setOptions(options: Map<Class<out Feature<*>>, String>): Boolean {
    featureFlow.update { it + options }
    return true
  }
}
