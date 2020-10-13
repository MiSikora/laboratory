package io.mehow.laboratory.inspector

internal data class FeatureGroup(
  val name: String,
  val fqcn: String,
  val models: List<FeatureModel>,
  val sources: List<FeatureModel>,
) {
  val hasFeatures = models.isNotEmpty()

  val description = models.firstOrNull()?.feature?.description.orEmpty()

  val hasMultipleSources = sources.size > 1

  val isCurrentSourceLocal = sources.firstOrNull(FeatureModel::isSelected)
    ?.feature
    ?.name
    ?.equals("Local", ignoreCase = true) ?: true
}
