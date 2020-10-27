package io.mehow.laboratory.datastore

import android.content.Context
import androidx.datastore.DataStore
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException
import java.io.File

internal class DataStoreFeatureStorage(
  private val dataStore: DataStore<FeatureFlags>,
) : FeatureStorage {
  override fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>) = dataStore
      .data
      .map { it.value[featureClass.name] }

  override suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>) = try {
    dataStore.data.first().value[featureClass.name]
  } catch (_: IOException) {
    null
  }

  override suspend fun <T : Feature<*>> setFeatures(vararg values: T) = try {
    dataStore.updateData { flags ->
      val updatedFeatures = flags.value + values.map { it.javaClass.name to it.name }.toMap()
      return@updateData flags.copy(value = updatedFeatures)
    }
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
public fun FeatureStorage.Companion.dataStore(fileProvider: () -> File): FeatureStorage {
  return dataStoreBuilder(fileProvider).build()
}

/**
 * Creates a [FeatureStorage] that is backed by [DataStore] with a file in an apps default directory.
 */
public fun FeatureStorage.Companion.dataStore(context: Context, fileName: String): FeatureStorage {
  return dataStoreBuilder(context, fileName).build()
}
