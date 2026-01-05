package io.mehow.laboratory.sharedpreferences

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import io.mehow.laboratory.Storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/** Creates a [Storage] that is backed by [SharedPreferences]. */
public fun Storage.Companion.sharedPreferences(preferences: SharedPreferences): Storage =
  SharedPreferencesStorage(preferences)

internal class SharedPreferencesStorage(private val preferences: SharedPreferences) : Storage {
  override suspend fun getString(key: String): String? = preferences.getStringSafe(key.stringKey)

  override suspend fun setString(key: String, value: String): Boolean {
    preferences.edit { putString(key.stringKey, value) }
    return true
  }

  override fun stringFlow(key: String): Flow<String?> {
    val stringKey = key.stringKey
    return callbackFlow {
        val listener = OnSharedPreferenceChangeListener { _, prefsKey ->
          if (prefsKey == stringKey) {
            trySend(preferences.getStringSafe(stringKey))
          }
        }
        send(preferences.getStringSafe(stringKey))
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
      }
      .conflate()
  }

  override suspend fun setStrings(entries: Map<String, String>): Boolean {
    preferences.edit {
      for ((key, value) in entries) {
        putString(key.stringKey, value)
      }
    }
    return true
  }

  override suspend fun getBoolean(key: String): Boolean? =
    preferences.getBooleanSafe(key.booleanKey)

  override fun booleanFlow(key: String): Flow<Boolean?> {
    val booleanKey = key.booleanKey
    return callbackFlow {
        val listener = OnSharedPreferenceChangeListener { _, prefsKey ->
          if (prefsKey == booleanKey) {
            trySend(preferences.getBooleanSafe(booleanKey))
          }
        }
        send(preferences.getBooleanSafe(booleanKey))
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
      }
      .conflate()
  }

  override suspend fun setBoolean(key: String, value: Boolean): Boolean {
    preferences.edit { putBoolean(key.booleanKey, value) }
    return true
  }

  override suspend fun setBooleans(entries: Map<String, Boolean>): Boolean {
    preferences.edit {
      for ((key, value) in entries) {
        putBoolean(key.booleanKey, value)
      }
    }
    return true
  }

  override suspend fun clear(): Boolean {
    preferences.edit {
      for (key in preferences.all.keys) {
        remove(key)
      }
    }
    return true
  }

  private val String.stringKey
    get() = "string:$this"

  private val String.booleanKey
    get() = "boolean:$this"

  private fun SharedPreferences.getStringSafe(key: String): String? {
    return try {
      getString(key, null)
    } catch (_: ClassCastException) {
      null
    }
  }

  private fun SharedPreferences.getBooleanSafe(key: String): Boolean? {
    return try {
      if (contains(key)) {
        getBoolean(key, false)
      } else {
        null
      }
    } catch (_: ClassCastException) {
      null
    }
  }

  private fun SharedPreferences.edit(block: SharedPreferences.Editor.() -> Unit) {
    edit().apply(block).apply()
  }
}
