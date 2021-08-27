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

internal class SharedPreferencesFeatureStorage(
  private val preferences: SharedPreferences,
) : FeatureStorage {
  override fun <T : Feature<*>> observeFeatureName(feature: Class<T>) = callbackFlow {
    val listener = OnSharedPreferenceChangeListener { _, key ->
      if (key == feature.name) @OptIn(ExperimentalCoroutinesApi::class) trySend(getStringSafe(key))
    }
    @OptIn(ExperimentalCoroutinesApi::class) trySend(getStringSafe(feature.name))
    preferences.registerOnSharedPreferenceChangeListener(listener)
    @OptIn(ExperimentalCoroutinesApi::class) awaitClose {
      preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
  }.conflate()

  override suspend fun <T : Feature<*>> getFeatureName(feature: Class<T>) = getStringSafe(feature.name)

  private fun getStringSafe(key: String) = try {
    preferences.getString(key, null)
  } catch (_: ClassCastException) {
    null
  }

  override suspend fun <T : Feature<*>> setOptions(vararg options: T): Boolean {
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

/**
 * Creates a [FeatureStorage] that is backed by [SharedPreferences] with a [fileName] in [private mode][MODE_PRIVATE].
 */
@Deprecated(
    "This function will be removed in 1.0.0. Use FeatureStorage.sharedPreferences(SharedPreferences) instead.",
)
public fun FeatureStorage.Companion.sharedPreferences(context: Context, fileName: String): FeatureStorage {
  val preferences = context.getSharedPreferences(fileName, MODE_PRIVATE)
  return sharedPreferences(preferences)
}
