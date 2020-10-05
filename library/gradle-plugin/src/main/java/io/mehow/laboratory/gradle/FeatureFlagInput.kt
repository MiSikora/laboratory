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

  fun withFallbackValue(value: String) {
    values += FeatureValue(value, isFallbackValue = true)
  }

  fun withSource(value: String) {
    sources += FeatureValue(value)
  }

  fun withFallbackSource(value: String) {
    sources += FeatureValue(value, isFallbackValue = true)
  }

  internal fun toBuilder(): FeatureFlagModel.Builder {
    return FeatureFlagModel.Builder(
      visibility = if (isPublic) Public else Internal,
      packageName = packageName ?: "",
      name = name,
      values = values,
      sourcedWithValues = sources
    )
  }
}
