package io.mehow.laboratory.sharedpreferences

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

internal class SharedPreferencesFeatureStorage(
  private val preferences: SharedPreferences,
) : FeatureStorage {
  override fun observeFeatureName(feature: Class<out Feature<*>>) = callbackFlow {
    val listener = OnSharedPreferenceChangeListener { _, key ->
      if (key == feature.name) trySend(getStringSafe(key))
    }
    send(getStringSafe(feature.name))
    preferences.registerOnSharedPreferenceChangeListener(listener)
    awaitClose {
      preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
  }.conflate()

  override suspend fun getFeatureName(feature: Class<out Feature<*>>) = getStringSafe(feature.name)

  private fun getStringSafe(key: String) = try {
    preferences.getString(key, null)
  } catch (_: ClassCastException) {
    null
  }

  override suspend fun setOptions(vararg options: Feature<*>): Boolean {
    preferences.edit {
      for (option in options) {
        putString(option.javaClass.name, option.name)
      }
    }
    return true
  }

  override suspend fun clear(): Boolean {
    val editor = preferences.edit()
    for (key in preferences.all.keys) {
      editor.remove(key)
    }
    editor.apply()
    return true
  }
}

/**
 * Creates a [FeatureStorage] that is backed by [SharedPreferences].
 */
public fun FeatureStorage.Companion.sharedPreferences(preferences: SharedPreferences): FeatureStorage {
  return SharedPreferencesFeatureStorage(preferences)
}
