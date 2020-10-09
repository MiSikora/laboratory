package io.mehow.laboratory.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

internal class FeaturesViewModel(
  configuration: Configuration,
) : ViewModel() {
  private val laboratory = configuration.laboratory
  private val groups = configuration.featureFactories.mapValues { (_, factory) -> factory.create() }
  private val emptyFlow = emptyFlow<List<FeatureGroup>>()

  suspend fun selectFeature(feature: Feature<*>) = laboratory.setFeature(feature)

  fun observeFeatureGroups(groupName: String): Flow<List<FeatureGroup>> {
    return groups.getValue(groupName)
      .filterNot { it.enumConstants.isNullOrEmpty() }
      .map(::observeFeatureGroup)
      .fold(emptyFlow, ::combineFeatureGroups)
      .dropWhile { features -> isAnyFeatureMissing(groupName, features) }
      .map { featureGroups -> featureGroups.sortedBy(FeatureGroup::name) }
  }

  private fun observeFeatureGroup(group: Class<Feature<*>>): Flow<FeatureGroup> {
    return laboratory.observe(group)
      .map(::createFeatureModels)
      .map { featureModels -> FeatureGroup(group.simpleReadableName, group.name, featureModels) }
      .filter { featureGroup -> featureGroup.hasFeatures }
  }

  private fun createFeatureModels(selectedFeature: Feature<*>): List<FeatureModel> {
    val availableFeatureValues = selectedFeature.javaClass.enumConstants.orEmpty()
    return availableFeatureValues
      .map { feature -> FeatureModel(feature, isSelected = feature == selectedFeature) }
      .let(::ensureOneModelSelected)
  }

  private val Class<Feature<*>>.simpleReadableName get() = name.substringAfterLast(".").replace('$', '.')

  private fun ensureOneModelSelected(models: List<FeatureModel>): List<FeatureModel> {
    return if (models.any(FeatureModel::isSelected)) models else models.selectFirst()
  }

  private fun List<FeatureModel>.selectFirst(): List<FeatureModel> {
    return take(1).map(FeatureModel::select) + drop(1)
  }

  private fun combineFeatureGroups(
    groups: Flow<List<FeatureGroup>>,
    group: Flow<FeatureGroup>,
  ): Flow<List<FeatureGroup>> = if (groups === emptyFlow) {
    group.map(::listOf)
  } else {
    groups.combine(group) { xs, x -> xs + x }
  }

  private fun isAnyFeatureMissing(groupName: String, features: List<FeatureGroup>): Boolean {
    return features.size != groups.getValue(groupName).filterNot { it.enumConstants.isNullOrEmpty() }.size
  }

  class Factory(private val configuration: Configuration) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      require(modelClass == FeaturesViewModel::class.java) { "Cannot create $modelClass" }
      @Suppress("UNCHECKED_CAST")
      return FeaturesViewModel(configuration) as T
    }
  }
}
