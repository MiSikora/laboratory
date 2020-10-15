package io.mehow.laboratory.datastore

import androidx.datastore.DataMigration
import androidx.datastore.DataStoreFactory
import androidx.datastore.Serializer
import androidx.datastore.handlers.ReplaceFileCorruptionHandler
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

class Builder internal constructor(
  private val produceFile: () -> File,
) {
  private var serializer: Serializer<FeatureFlags> = FeatureFlagsSerializer
  private var corruptionHandler: ReplaceFileCorruptionHandler<FeatureFlags>? = null
  private var migrations: List<DataMigration<FeatureFlags>> = emptyList()
  private var coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  fun featureSerializer(serializer: Serializer<FeatureFlags>) = apply {
    this.serializer = serializer
  }

  fun corruptionHandler(handler: ReplaceFileCorruptionHandler<FeatureFlags>) = apply {
    this.corruptionHandler = handler
  }

  fun migrations(migrations: Iterable<DataMigration<FeatureFlags>>) = apply {
    this.migrations = migrations.toList()
  }

  fun migrations(vararg migrations: DataMigration<FeatureFlags>) = apply {
    this.migrations = migrations.toList()
  }

  fun coroutineScope(scope: CoroutineScope) = apply {
    this.coroutineScope = scope
  }

  fun build(): FeatureStorage {
    val dataStore = DataStoreFactory.create(
        produceFile = produceFile,
        serializer = serializer,
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = coroutineScope,
    )
    return DataStoreFeatureStorage(dataStore)
  }
}

fun FeatureStorage.Companion.dataStoreBuilder(produceFile: () -> File): Builder {
  return Builder(produceFile)
}
