package io.mehow.laboratory.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import io.mehow.laboratory.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Creates a [Storage] that is backed by [DataStore]. */
public fun Storage.Companion.dataStore(dataStore: DataStore<StorageData>): Storage =
  DataStoreStorage(dataStore)

internal class DataStoreStorage(private val dataStore: DataStore<StorageData>) : Storage {
  override suspend fun getString(key: String): String? {
    return try {
      dataStore.data.first().strings[key]
    } catch (_: IOException) {
      null
    }
  }

  override fun stringFlow(key: String): Flow<String?> {
    return dataStore.data.map { data -> data.strings[key] }.distinctUntilChanged()
  }

  override suspend fun setString(key: String, value: String): Boolean {
    return try {
      dataStore.updateData { data -> data.copy(strings = data.strings + (key to value)) }
      true
    } catch (_: IOException) {
      false
    }
  }

  override suspend fun setStrings(entries: Map<String, String>): Boolean {
    return try {
      dataStore.updateData { data -> data.copy(strings = data.strings + entries) }
      true
    } catch (_: IOException) {
      false
    }
  }

  override suspend fun getBoolean(key: String): Boolean? {
    return try {
      dataStore.data.first().booleans[key]
    } catch (_: IOException) {
      null
    }
  }

  override fun booleanFlow(key: String): Flow<Boolean?> {
    return dataStore.data.map { data -> data.booleans[key] }.distinctUntilChanged()
  }

  override suspend fun setBoolean(key: String, value: Boolean): Boolean {
    return try {
      dataStore.updateData { data -> data.copy(booleans = data.booleans + (key to value)) }
      true
    } catch (_: IOException) {
      false
    }
  }

  override suspend fun setBooleans(entries: Map<String, Boolean>): Boolean {
    return try {
      dataStore.updateData { data -> data.copy(booleans = data.booleans + entries) }
      true
    } catch (_: IOException) {
      false
    }
  }

  override suspend fun clear(): Boolean {
    return try {
      dataStore.updateData { StorageData() }
      true
    } catch (_: IOException) {
      false
    }
  }
}
