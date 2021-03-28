package io.mehow.laboratory.inspector

import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal fun InspectorViewModel.observeSelectedFeaturesAndSources() = sectionFlow().map { groups ->
  groups.map { group ->
    val option = group.models.single(OptionUiModel::isSelected).option
    val source = group.sources.singleOrNull(OptionUiModel::isSelected)?.option
    option to source
  }
}

internal fun InspectorViewModel.observeSelectedFeatures() =
  observeSelectedFeaturesAndSources().map { pairs ->
    pairs.map { (feature, _) -> feature }
  }

internal fun InspectorViewModel.observeFeatureClasses() = sectionFlow().map { groups ->
  groups.map { group -> group.models.first().option::class }
}

internal fun InspectorViewModel.observeSelectedFeaturesAndEnabledState() = sectionFlow().map { groups ->
  groups.map { group ->
    val option = group.models.single(OptionUiModel::isSelected).option
    option to group.isEnabled
  }
}

internal val InspectorViewModel.Companion.defaultSection get() = "section"

@Suppress("TestFunctionName")
internal fun InspectorViewModel(
  laboratory: Laboratory,
  searchQueries: Flow<SearchQuery>,
  featureFactory: FeatureFactory,
  deprecationHandler: DeprecationHandler,
) = InspectorViewModel(
    laboratory,
    searchQueries,
    mapOf(InspectorViewModel.defaultSection to featureFactory),
    deprecationHandler,
)

internal fun InspectorViewModel.sectionFlow() = sectionFlow(InspectorViewModel.defaultSection)

internal fun InspectorViewModel.supervisedFeaturesFlow(
  sectionName: String = InspectorViewModel.defaultSection,
) = sectionFlow(sectionName).map { uiModels ->
  uiModels.flatMap { it.models }.map { it.option to it.supervisedFeatures }
}
