package io.mehow.laboratory.android

import android.content.SharedPreferences
import androidx.core.content.edit
import io.mehow.laboratory.SubjectStorage

class SharedPreferencesSubjectStorage(
  private val preferences: SharedPreferences
) : SubjectStorage {
  override fun <T : Enum<*>> getSubjectName(group: Class<T>) = try {
    preferences.getString(group.name, null)
  } catch (_: ClassCastException) {
    null
  }

  override fun <T : Enum<*>> setSubject(subject: T) = preferences.edit {
    putString(subject.javaClass.name, subject.name)
  }
}
