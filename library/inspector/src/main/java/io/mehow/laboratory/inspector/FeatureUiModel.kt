package io.mehow.laboratory.inspector

import io.mehow.laboratory.Feature
import io.mehow.laboratory.supervisorOption

internal data class FeatureUiModel(
  val type: Class<out Feature<*>>,
  val name: String,
  val description: List<TextToken>,
  val models: List<OptionUiModel>,
  val sources: List<OptionUiModel>,
  val deprecationAlignment: DeprecationAlignment?,
  val deprecationPhenotype: DeprecationPhenotype?,
  val supervisorOption: Feature<*>?,
) {
  val hasMultipleSources = sources.size > 1

  private val isCurrentSourceLocal = sources.firstOrNull(OptionUiModel::isSelected)
      ?.option
      ?.name
      ?.equals("Local", ignoreCase = true) ?: true

  private val isSupervised = type.supervisorOption == supervisorOption

  val isEnabled = isCurrentSourceLocal && isSupervised

  companion object {
    private val firstAlignmentOrdinal = DeprecationAlignment.values().first()

    val NaturalComparator = compareBy<FeatureUiModel>(
        { it.deprecationAlignment ?: firstAlignmentOrdinal },
        { it.name }
    )
  }
}

internal fun List<FeatureUiModel>.search(query: SearchQuery) = mapNotNull { uiModels ->
  uiModels.search(query)
}

private fun FeatureUiModel.search(query: SearchQuery) = takeIf {
  query.matches(name) || query.matches(modelNames) || query.matches(sourceNames)
}

private val FeatureUiModel.modelNames get() = models.map { it.option.name }

private val FeatureUiModel.sourceNames get() = sources.map { it.option.name }
