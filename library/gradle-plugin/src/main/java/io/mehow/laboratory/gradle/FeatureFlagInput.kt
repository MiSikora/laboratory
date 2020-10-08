package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.FeatureValue
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

class FeatureFlagInput internal constructor(private val name: String) {
  var isPublic: Boolean = true
  var packageName: String? = null
  private val values: MutableList<FeatureValue> = mutableListOf()
  private val sources: MutableList<FeatureValue> = mutableListOf()

  fun withValue(value: String) {
    values += FeatureValue(value)
  }

  fun withDefaultValue(value: String) {
    values += FeatureValue(value, isDefaultValue = true)
  }

  fun withSource(value: String) {
    sources += FeatureValue(value)
  }

  fun withDefaultSource(value: String) {
    sources += FeatureValue(value, isDefaultValue = true)
  }

  internal fun toBuilder(): FeatureFlagModel.Builder {
    return FeatureFlagModel.Builder(
      visibility = if (isPublic) Public else Internal,
      packageName = packageName ?: "",
      names = listOf(name),
      values = values,
      sourceValues = sources,
    )
  }
}
