package io.mehow.laboratory.datastore

import androidx.datastore.core.DataStore
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException

internal class DataStoreFeatureStorage(
  private val dataStore: DataStore<FeatureFlags>,
) : FeatureStorage {
  override fun observeFeatureName(feature: Class<out Feature<*>>) = dataStore
      .data
      .map { it.options[feature.name] }

  override suspend fun getFeatureName(feature: Class<out Feature<*>>) = try {
    dataStore.data.first().options[feature.name]
  } catch (_: IOException) {
    null
  }

  override suspend fun setOptions(
    vararg options: Feature<*>,
  ) = setOptions(options.associate { it.javaClass.name to it.name })

  override suspend fun setOptions(
    options: Collection<Feature<*>>,
  ) = setOptions(options.associate { it.javaClass.name to it.name })

  private suspend fun setOptions(options: Map<String, String>) = try {
    dataStore.updateData { flags ->
      val updatedFeatures = flags.options + options
      flags.copy(options = updatedFeatures)
    }
    true
  } catch (_: IOException) {
    false
  }

  override suspend fun clear() = try {
    dataStore.updateData { FeatureFlags() }
    true
  } catch (_: IOException) {
    false
  }
}

/**
 * Creates a [FeatureStorage] that is backed by [DataStore].
 */
public fun FeatureStorage.Companion.dataStore(dataStore: DataStore<FeatureFlags>): FeatureStorage {
  return DataStoreFeatureStorage(dataStore)
}
