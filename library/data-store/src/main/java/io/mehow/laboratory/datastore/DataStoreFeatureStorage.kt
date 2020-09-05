package io.mehow.laboratory.datastore

import androidx.datastore.DataMigration
import androidx.datastore.DataStoreFactory
import androidx.datastore.Serializer
import androidx.datastore.handlers.ReplaceFileCorruptionHandler
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException
import java.io.File

@Suppress("LongParameterList")
class DataStoreFeatureStorage @JvmOverloads constructor(
  produceFile: () -> File,
  serializer: Serializer<FeatureFlags> = FeatureFlagsSerializer,
  corruptionHandler: ReplaceFileCorruptionHandler<FeatureFlags>? = null,
  migrations: List<DataMigration<FeatureFlags>> = listOf(),
  scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : FeatureStorage {
  private val dataStore = DataStoreFactory.create(
    produceFile = produceFile,
    serializer = serializer,
    corruptionHandler = corruptionHandler,
    migrations = migrations,
    scope = scope,
  )

  override fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>) = dataStore
    .data
    .map { it.value[featureClass.name] }

  override suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>) = try {
    dataStore.data.first().value[featureClass.name]
  } catch (_: IOException) {
    null
  }

  override suspend fun <T : Feature<*>> setFeature(feature: T) = try {
    dataStore.updateData { flags ->
      val updatedFeatures = flags.value + (feature.javaClass.name to feature.name)
      return@updateData flags.copy(value = updatedFeatures)
    }
    true
  } catch (_: IOException) {
    false
  }
}
