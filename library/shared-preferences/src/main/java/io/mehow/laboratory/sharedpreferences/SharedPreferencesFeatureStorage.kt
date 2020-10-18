package io.mehow.laboratory.sharedpreferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
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
  override fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>) = callbackFlow {
    val listener = OnSharedPreferenceChangeListener { _, key ->
      if (key == featureClass.name) offer(getStringSafe(key))
    }
    offer(getStringSafe(featureClass.name))
    preferences.registerOnSharedPreferenceChangeListener(listener)
    awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
  }.conflate()

  override suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>) = getStringSafe(featureClass.name)

  private fun getStringSafe(key: String) = try {
    preferences.getString(key, null)
  } catch (_: ClassCastException) {
    null
  }

  override suspend fun <T : Feature<*>> setFeatures(vararg features: T): Boolean {
    preferences.edit {
      for (feature in features) {
        putString(feature.javaClass.name, feature.name)
      }
    }
    return true
  }
}

fun FeatureStorage.Companion.sharedPreferences(preferences: SharedPreferences): FeatureStorage {
  return SharedPreferencesFeatureStorage(preferences)
}

fun FeatureStorage.Companion.sharedPreferences(context: Context, name: String): FeatureStorage {
  val preferences = context.getSharedPreferences(name, MODE_PRIVATE)
  return sharedPreferences(preferences)
}
