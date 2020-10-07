package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.SourcedFeatureStorageModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

class SourcedFeatureStorageInput internal constructor() {
  var isPublic: Boolean = false
  var packageName: String? = null
  internal var projectFilter: ProjectFilter
    private set
  var generateFactory: Boolean = false

  init {
    projectFilter = ProjectFilter { false }
  }

  fun excludeProjects(filter: ProjectFilter) {
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
