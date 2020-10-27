package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.FeatureValue
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

/**
 * Representation of a generate feature flag. It must have at least one value and exactly one default value.
 */
public class FeatureFlagInput internal constructor(
  private val name: String,
) {
  /**
   * Sets whether the generated feature flag should be public or internal.
   */
  public var isPublic: Boolean = true

  /**
   * Sets package name of the generated feature flag. Overwrites any previously set values.
   */
  public var packageName: String? = null

  /**
   * Sets description of the generated feature flag. Overwrites any previously set values.
   */
  public var description: String? = null

  private val values: MutableList<FeatureValue> = mutableListOf()

  private val sources: MutableList<FeatureValue> = mutableListOf()

  /**
   * Adds a feature value.
   */
  public fun withValue(value: String) {
    values += FeatureValue(value)
  }

  /**
   * Adds a feature value that will be used as a default value.
   * Exactly one value must be set with this method.
   */
  public fun withDefaultValue(value: String) {
    values += FeatureValue(value, isDefaultValue = true)
  }

  /**
   * Adds a feature flag source. Any sources that are named "Local", or any variation of this word,
   * will be filtered out.
   */
  public fun withSource(value: String) {
    sources += FeatureValue(value)
  }

  /**
   * Adds a feature flag source that will be used a default source. Any sources that are named "Local",
   * or any variation of this word, will be filtered out.
   * At most one value can be set with this method.
   */
  public fun withDefaultSource(value: String) {
    sources += FeatureValue(value, isDefaultValue = true)
  }

  internal fun toBuilder(): FeatureFlagModel.Builder {
    return FeatureFlagModel.Builder(
        visibility = if (isPublic) Public else Internal,
        packageName = packageName ?: "",
        names = listOf(name),
        values = values,
        sourceValues = sources,
        description = description.orEmpty(),
    )
  }
}
