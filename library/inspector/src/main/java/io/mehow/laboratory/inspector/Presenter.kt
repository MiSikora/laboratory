package io.mehow.laboratory.inspector

import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory

internal class Presenter(
  factory: FeatureFactory,
  private val laboratory: Laboratory,
) {
  private val groups = factory.create()

  suspend fun getFeatureGroups(): List<FeatureGroup> {
    return groups
      .sortedBy(::groupName)
      .map { createFeatureGroup(it) }
      .filter(FeatureGroup::hasFeatures)
  }

  suspend fun selectFeature(feature: Feature<*>) = laboratory.setFeature(feature)

  private fun groupName(group: Class<Feature<*>>): String = group.simpleName

  private suspend fun createFeatureGroup(group: Class<Feature<*>>): FeatureGroup {
    return FeatureGroup(groupName(group), getFeatureModels(group))
  }

  private suspend fun getFeatureModels(group: Class<Feature<*>>): List<FeatureModel> {
    val selectedFeature = laboratory.experiment(group)
    return group.enumConstants
      .orEmpty()
      .map { feature -> FeatureModel(feature, isSelected = feature == selectedFeature) }
      .let(::ensureOneModelSelected)
  }

  private fun ensureOneModelSelected(models: List<FeatureModel>): List<FeatureModel> {
    return if (models.any(FeatureModel::isSelected)) models else models.selectFirst()
  }

  private fun List<FeatureModel>.selectFirst(): List<FeatureModel> {
    return take(1).map(FeatureModel::select) + drop(1)
  }
}
