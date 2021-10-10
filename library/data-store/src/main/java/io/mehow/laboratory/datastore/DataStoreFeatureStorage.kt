package io.mehow.laboratory.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException
import java.io.File

internal class DataStoreFeatureStorage(
  private val dataStore: DataStore<FeatureFlags>,
) : FeatureStorage {
  override fun observeFeatureName(feature: Class<out Feature<*>>) = dataStore
      .data
      .map { it.value[feature.name] }

  override suspend fun getFeatureName(feature: Class<out Feature<*>>) = try {
    dataStore.data.first().value[feature.name]
  } catch (_: IOException) {
    null
  }

  override suspend fun setOptions(vararg options: Feature<*>) = try {
    dataStore.updateData { flags ->
      val updatedFeatures = flags.value + options.associate { it.javaClass.name to it.name }
      return@updateData flags.copy(value = updatedFeatures)
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

/**
 * Creates a [FeatureStorage] that is backed by [DataStore] with a file taken from [fileProvider].
 */
@Deprecated(
    "This function will be removed in 1.0.0. Use FeatureStorage.dataStore(DataStore) instead.",
)
public fun FeatureStorage.Companion.dataStore(fileProvider: () -> File): FeatureStorage {
  return dataStore(DataStoreFactory.create(FeatureFlagsSerializer, produceFile = fileProvider))
}

/**
 * Creates a [FeatureStorage] that is backed by [DataStore] with a file in an apps default directory.
 */
@Deprecated(
    "This function will be removed in 1.0.0. Use FeatureStorage.dataStore(DataStore) instead.",
)
public fun FeatureStorage.Companion.dataStore(context: Context, fileName: String): FeatureStorage {
  return dataStore(DataStoreFactory.create(FeatureFlagsSerializer) { File(context.filesDir, "datastore/$fileName") })
}
