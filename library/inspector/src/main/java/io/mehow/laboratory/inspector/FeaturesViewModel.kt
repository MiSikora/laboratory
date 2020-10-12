package io.mehow.laboratory.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class FeaturesViewModel(
  configuration: Configuration,
) : ViewModel() {
  private val laboratory = configuration.laboratory
  private val groups = configuration.featureFactories.mapValues { (_, factory) -> factory.create() }
  private val emptyFlow = emptyFlow<List<FeatureGroup>>()

  suspend fun selectFeature(feature: Feature<*>) = laboratory.setFeature(feature)

  fun observeFeatureGroups(groupName: String) = flow {
    val listGroupFlow = withContext(Dispatchers.Default) {
      groups.getValue(groupName)
        .filterNot { it.enumConstants.isNullOrEmpty() }
        .map { it.observeFeatureGroup() }
        .fold(emptyFlow, ::combineFeatureGroups)
    }.flowOn(Dispatchers.Default).map { featureGroups ->
      featureGroups.sortedBy(FeatureGroup::name)
    }
    emitAll(listGroupFlow)
  }

  suspend fun resetAllFeatures() = withContext(Dispatchers.Default) {
    groups.values.flatten()
      .flatMap { featureGroup ->
        val featureValues = featureGroup.enumConstants.orEmpty()
        val sourceValues = featureGroup.sourceClass?.enumConstants.orEmpty()
        return@flatMap listOf(featureValues, sourceValues)
      }
      .filter { it.isNotEmpty() }
      .map { features -> features.firstOrNull { it.isDefaultValue } ?: features.first() }
      .toTypedArray()
      .let { laboratory.setFeatures(*it) }
  }

  private val Class<Feature<*>>.sourceClass get() = enumConstants?.firstOrNull()?.sourcedWith

  private fun Class<Feature<*>>.observeFeatureGroup(): Flow<FeatureGroup> {
    val sources = sourceClass?.observeFeatureModels() ?: flowOf(emptyList())
    val features = observeFeatureModels()
    return combine(features, sources) { features, sources ->
      FeatureGroup(simpleReadableName, name, features, sources)
    }.filter { featureGroup -> featureGroup.hasFeatures }
  }

  private fun Class<Feature<*>>.observeFeatureModels() = laboratory.observe(this).map(::createFeatureModels)

  private fun createFeatureModels(selectedFeature: Feature<*>) = selectedFeature.javaClass
    .enumConstants
    .orEmpty()
    .map { feature -> FeatureModel(feature, isSelected = feature == selectedFeature) }
    .ensureOneModelSelected()

  private val Class<Feature<*>>.simpleReadableName get() = name.substringAfterLast(".").replace('$', '.')

  private fun List<FeatureModel>.ensureOneModelSelected() = if (any(FeatureModel::isSelected)) {
    this
  } else {
    selectFirst()
  }

  private fun List<FeatureModel>.selectFirst() = take(1).map(FeatureModel::select) + drop(1)

  private fun combineFeatureGroups(
    groups: Flow<List<FeatureGroup>>,
    group: Flow<FeatureGroup>,
  ): Flow<List<FeatureGroup>> = if (groups === emptyFlow) {
    group.map(::listOf)
  } else {
    groups.combine(group) { xs, x -> xs + x }
  }

  class Factory(private val configuration: Configuration) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      require(modelClass == FeaturesViewModel::class.java) { "Cannot create $modelClass" }
      @Suppress("UNCHECKED_CAST")
      return FeaturesViewModel(configuration) as T
    }
  }
}
