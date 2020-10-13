package io.mehow.laboratory

import kotlinx.coroutines.flow.Flow

interface FeatureStorage {
  fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>): Flow<String?>
  suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>): String?
  @JvmDefault suspend fun <T : Feature<*>> setFeature(feature: T): Boolean = setFeatures(feature)
  suspend fun <T : Feature<*>> setFeatures(vararg features: T): Boolean

  companion object {
    fun inMemory(): FeatureStorage = InMemoryFeatureStorage()

    fun sourced(
      localSource: FeatureStorage,
      remoteSources: Map<String, FeatureStorage>,
    ): FeatureStorage = SourcedFeatureStorage(localSource, remoteSources)
  }
}

