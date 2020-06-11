package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

class FeatureFlagInput internal constructor(private val name: String) {
  var isPublic: Boolean = true
  var packageName: String? = null
  private val values: MutableList<String> = mutableListOf()

  fun withValue(value: String) {
    values += value
  }

  internal fun toBuilder(): FeatureFlagModel.Builder {
    return FeatureFlagModel.Builder(
      visibility = if (isPublic) Public else Internal,
      packageName = packageName ?: "",
      name = name,
      values = values
    )
  }
}
