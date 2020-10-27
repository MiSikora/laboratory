package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.SourcedFeatureStorageModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

/**
 * Representation of a generated feature storage that is aware of feature flags sources.
 */
public class SourcedFeatureStorageInput internal constructor() {
  /**
   * Sets whether the generated feature storage should be public or internal.
   */
  public var isPublic: Boolean = false

  /**
   * Sets package name of the generated feature storage. Overwrites any previously set values.
   */
  public var packageName: String? = null

  internal var projectFilter = ProjectFilter { false }
    private set

  /**
   * Sets which Gradle projects should be excluded from contributing
   * their feature flag sources to the generated feature storage.
   */
  public fun excludeProjects(filter: ProjectFilter) {
    projectFilter = filter
  }

  internal fun toBuilder(sourceNames: List<String>): SourcedFeatureStorageModel.Builder {
    return SourcedFeatureStorageModel.Builder(
        visibility = if (isPublic) Public else Internal,
        packageName = packageName ?: "",
        sourceNames = sourceNames,
    )
  }
}
