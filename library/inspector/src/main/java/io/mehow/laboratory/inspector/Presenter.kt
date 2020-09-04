package io.mehow.laboratory.inspector

import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage

internal class Presenter(
  factory: FeatureFactory,
  private val storage: FeatureStorage,
) {
  private val groups = factory.create()

  fun getFeatureGroups(): List<FeatureGroup> {
    return groups
      .sortedBy(::groupName)
      .map(::createFeatureGroup)
      .filter(FeatureGroup::hasFeatures)
  }

  fun selectFeature(feature: Enum<*>) = storage.setFeature(feature)

  private fun groupName(group: Class<Enum<*>>): String = group.simpleName

  private fun createFeatureGroup(group: Class<Enum<*>>): FeatureGroup {
    return FeatureGroup(groupName(group), getFeatureModels(group))
  }

  private fun getFeatureModels(group: Class<Enum<*>>): List<FeatureModel> {
    val featureName = storage.getFeatureName(group)
    return group.enumConstants
      .orEmpty()
      .map { feature -> FeatureModel(feature, isSelected = feature.name == featureName) }
      .let(::ensureOneModelSelected)
  }

  private fun ensureOneModelSelected(models: List<FeatureModel>): List<FeatureModel> {
    return if (models.any(FeatureModel::isSelected)) models else models.selectFirst()
  }

  private fun List<FeatureModel>.selectFirst(): List<FeatureModel> {
    return take(1).map(FeatureModel::select) + drop(1)
  }
}
