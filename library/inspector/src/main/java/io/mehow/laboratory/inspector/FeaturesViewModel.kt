package io.mehow.laboratory.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration

internal class FeaturesViewModel(
  configuration: Configuration,
) : ViewModel() {
  private val laboratory = configuration.laboratory
  private val groups = configuration.featureFactories.mapValues { (_, factory) -> factory.create() }

  suspend fun getFeatureGroups(groupName: String): List<FeatureGroup> {
    return groups.getValue(groupName)
      .filterNot { it.enumConstants.isNullOrEmpty() }
      .sortedBy(::groupName)
      .map { createFeatureGroup(it) }
      .filter(FeatureGroup::hasFeatures)
  }

  suspend fun selectFeature(feature: Feature<*>) = laboratory.setFeature(feature)

  private fun groupName(group: Class<Feature<*>>): String = group.name
    .substringAfterLast(".")
    .replace('$', '.')

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

  class Factory(private val configuration: Configuration) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      require(modelClass == FeaturesViewModel::class.java) { "Cannot create $modelClass" }
      @Suppress("UNCHECKED_CAST")
      return FeaturesViewModel(configuration) as T
    }
  }
}
