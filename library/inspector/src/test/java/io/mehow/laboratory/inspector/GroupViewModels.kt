package io.mehow.laboratory.inspector

import kotlinx.coroutines.flow.map

internal fun GroupViewModel.observeSelectedFeaturesAndSources() = observeFeatureGroup().map { groups ->
  groups.map { group ->
    val option = group.models.single(OptionUiModel::isSelected).option
    val source = group.sources.singleOrNull(OptionUiModel::isSelected)?.option
    option to source
  }
}

internal fun GroupViewModel.observeSelectedFeatures() = observeSelectedFeaturesAndSources().map { pairs ->
  pairs.map { (feature, _) -> feature }
}

internal fun GroupViewModel.observeFeatureClasses() = observeFeatureGroup().map { groups ->
  groups.map { group -> group.models.first().option::class }
}
