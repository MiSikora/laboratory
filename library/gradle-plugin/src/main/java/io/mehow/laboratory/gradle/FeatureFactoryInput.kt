package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFactoryModel
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

/**
 * Representation of a generated feature factory class.
 */
class FeatureFactoryInput internal constructor() {
  /**
   * Sets whether the generated feature factory should be public or internal.
   */
  var isPublic: Boolean = false

  /**
   * Sets package name of the generated feature factory. Overwrites any previously set values.
   */
  var packageName: String? = null

  internal var projectFilter = ProjectFilter { false }
    private set

  /**
   * Sets which Gradle projects should be excluded from contributing
   * their feature flags to the generated feature factory.
   */
  fun excludeProjects(filter: ProjectFilter) {
    projectFilter = filter
  }

  internal fun toBuilder(features: List<FeatureFlagModel>): FeatureFactoryModel.Builder {
    return FeatureFactoryModel.Builder(
        visibility = if (isPublic) Public else Internal,
        packageName = packageName ?: "",
        features = features,
    )
  }
}
