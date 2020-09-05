package io.mehow.laboratory.android

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class SharedPreferencesFeatureStorage(
  private val preferences: SharedPreferences,
) : FeatureStorage {
  @OptIn(ExperimentalCoroutinesApi::class)
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

  override suspend fun <T : Feature<*>> setFeature(feature: T): Boolean {
    preferences.edit {
      putString(feature.javaClass.name, feature.name)
    }
    return true
  }
}
