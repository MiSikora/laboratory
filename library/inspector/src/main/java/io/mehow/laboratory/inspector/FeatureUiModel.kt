package io.mehow.laboratory.inspector

internal data class FeatureUiModel(
  val name: String,
  val fqcn: String,
  val models: List<OptionUiModel>,
  val sources: List<OptionUiModel>,
) {
  val description = models.firstOrNull()?.option?.description.orEmpty()

  val hasMultipleSources = sources.size > 1

  val isCurrentSourceLocal = sources.firstOrNull(OptionUiModel::isSelected)
      ?.option
      ?.name
      ?.equals("Local", ignoreCase = true) ?: true
}
