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

internal fun List<FeatureUiModel>.search(query: SearchQuery) = mapNotNull { uiModels ->
  uiModels.search(query)
}

private fun FeatureUiModel.search(query: SearchQuery) = takeIf {
  query.matches(name) || query.matches(modelNames) || query.matches(sourceNames)
}

private val FeatureUiModel.modelNames get() = models.map { it.option.name }

private val FeatureUiModel.sourceNames get() = sources.map { it.option.name }
