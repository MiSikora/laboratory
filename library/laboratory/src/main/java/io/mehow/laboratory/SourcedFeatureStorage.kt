package io.mehow.laboratory

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty

internal class SourcedFeatureStorage(
  private val localSource: FeatureStorage,
  private val remoteSources: Map<String, FeatureStorage>,
) : FeatureStorage {
  private val localLaboratory = Laboratory(localSource)

  @ExperimentalCoroutinesApi
  override fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>) = featureClass.observeSource()
      .map { source -> remoteSources[source.name] ?: localSource }
      .onEmpty { emit(localSource) }
      .flatMapLatest { storage -> storage.observeFeatureName(featureClass) }

  override suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>): String? {
    val storage = featureClass.getSource()?.let { remoteSources[it.name] } ?: localSource
    return storage.getFeatureName(featureClass)
  }

  override suspend fun <T : Feature<*>> setFeatures(vararg values: T) = localSource.setFeatures(*values)

  private fun <T : Feature<*>> Class<T>.observeSource() = validatedSource()
      ?.let { localLaboratory.observe(it) }
      ?: emptyFlow()

  private suspend fun <T : Feature<*>> Class<T>.getSource() = validatedSource()
      ?.let { localLaboratory.experiment(it) }

  private fun <T : Feature<*>> Class<T>.validatedSource() = enumConstants
      ?.firstOrNull()
      ?.sourcedWith
      ?.takeUnless { it.enumConstants.isNullOrEmpty() }
}
