package io.mehow.laboratory.sharedpreferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

@ExperimentalCoroutinesApi
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
@ExperimentalCoroutinesApi
public fun FeatureStorage.Companion.sharedPreferences(preferences: SharedPreferences): FeatureStorage {
  return SharedPreferencesFeatureStorage(preferences)
}

/**
 * Creates a [FeatureStorage] that is backed by [SharedPreferences] with a [fileName] in [private mode][MODE_PRIVATE].
 */
@Deprecated(
    "This function will be removed in 1.0.0. Use FeatureStorage.sharedPreferences(SharedPreferences) instead.",
)
@ExperimentalCoroutinesApi
public fun FeatureStorage.Companion.sharedPreferences(context: Context, fileName: String): FeatureStorage {
  val preferences = context.getSharedPreferences(fileName, MODE_PRIVATE)
  return sharedPreferences(preferences)
}
