package io.mehow.laboratory.android

import android.content.SharedPreferences
import androidx.core.content.edit
import io.mehow.laboratory.FeatureStorage

class SharedPreferencesFeatureStorage(
  private val preferences: SharedPreferences,
) : FeatureStorage {
  override suspend fun <T : Enum<*>> getFeatureName(group: Class<T>) = try {
    preferences.getString(group.name, null)
  } catch (_: ClassCastException) {
    null
  }

  override suspend fun <T : Enum<*>> setFeature(feature: T): Boolean {
    preferences.edit {
      putString(feature.javaClass.name, feature.name)
    }
    return true
  }
}
