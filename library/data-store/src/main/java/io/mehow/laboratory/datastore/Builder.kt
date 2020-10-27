package io.mehow.laboratory.datastore

import android.content.Context
import androidx.datastore.DataMigration
import androidx.datastore.DataStore
import androidx.datastore.DataStoreFactory
import androidx.datastore.Serializer
import androidx.datastore.handlers.ReplaceFileCorruptionHandler
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * Builder for a [FeatureStorage] backed by [DataStore].
 */
public class Builder internal constructor(
  private val fileProvider: () -> File,
) {
  private var serializer: Serializer<FeatureFlags> = FeatureFlagsSerializer
  private var corruptionHandler: ReplaceFileCorruptionHandler<FeatureFlags>? = null
  private var migrations: List<DataMigration<FeatureFlags>> = emptyList()
  private var coroutineScope: CoroutineScope? = null

  /**
   * Sets serializer that will be used by backing [DataStore].
   */
  public fun featureSerializer(serializer: Serializer<FeatureFlags>): Builder = apply {
    this.serializer = serializer
  }

  /**
   * Sets corruption handler that will be used by backing [DataStore].
   */
  public fun corruptionHandler(handler: ReplaceFileCorruptionHandler<FeatureFlags>): Builder = apply {
    this.corruptionHandler = handler
  }

  /**
   * Sets migrations that will be used by backing [DataStore].
   */
  public fun migrations(migrations: Iterable<DataMigration<FeatureFlags>>): Builder = apply {
    this.migrations = migrations.toList()
  }

  /**
   * Sets migrations that will be used by backing [DataStore].
   */
  public fun migrations(vararg migrations: DataMigration<FeatureFlags>): Builder = apply {
    this.migrations = migrations.toList()
  }

  /**
   * Sets coroutine scope that will be used by backing [DataStore].
   */
  public fun coroutineScope(scope: CoroutineScope): Builder = apply {
    this.coroutineScope = scope
  }

  /**
   * Creates a new instances of [FeatureStorage] backed by [DataStore] that uses parameters
   * defined in this builder.
   */
  public fun build(): FeatureStorage {
    val dataStore = DataStoreFactory.create(
        produceFile = fileProvider,
        serializer = serializer,
        corruptionHandler = corruptionHandler,
        migrations = migrations,
        scope = coroutineScope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )
    return DataStoreFeatureStorage(dataStore)
  }
}

/**
 * Creates a [FeatureStorage builder][Builder] with a file taken from [fileProvider].
 */
public fun FeatureStorage.Companion.dataStoreBuilder(fileProvider: () -> File): Builder {
  return Builder(fileProvider)
}

/**
 * Creates a [FeatureStorage builder][Builder] with a file in an apps default directory.
 */
public fun FeatureStorage.Companion.dataStoreBuilder(context: Context, fileName: String): Builder {
  return dataStoreBuilder { File(context.filesDir, "datastore/$fileName") }
}
