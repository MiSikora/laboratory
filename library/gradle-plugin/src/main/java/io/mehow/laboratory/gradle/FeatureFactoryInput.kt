package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFactoryModel
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

class FeatureFactoryInput internal constructor() {
  var isPublic: Boolean = false
  var packageName: String? = null
  internal var projectFilter: ProjectFilter
    private set

  init {
    projectFilter = ProjectFilter { false }
  }

  fun excludeProjects(filter: ProjectFilter) {
    projectFilter = filter
  }

  internal fun toBuilder(features: List<FeatureFlagModel>): FeatureFactoryModel.Builder {
    return FeatureFactoryModel.Builder(
      visibility = if (isPublic) Public else Internal,
      packageName = packageName ?: "",
      features = features
    )
  }
}
