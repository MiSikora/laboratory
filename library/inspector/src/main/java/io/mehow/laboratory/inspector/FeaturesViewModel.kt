package io.mehow.laboratory.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class FeaturesViewModel(
  configuration: Configuration,
) : ViewModel() {
  private val laboratory = configuration.laboratory
  private val metadataProvider = FeatureMetadata.Provider(configuration.featureFactories)
  private val emptyFlow = emptyFlow<List<FeatureGroup>>()

  suspend fun selectFeature(feature: Feature<*>) = laboratory.setFeature(feature)

  fun observeFeatureGroups(section: String) = flow {
    val listGroupFlow = withContext(Dispatchers.Default) {
      metadataProvider[section]
        .map { it.observeGroup(laboratory) }
        .fold(emptyFlow, ::combineFeatureGroups)
    }.flowOn(Dispatchers.Default).map { featureGroups ->
      featureGroups.sortedBy(FeatureGroup::name)
    }
    emitAll(listGroupFlow)
  }

  suspend fun resetAllFeatures() = withContext(Dispatchers.Default) {
    val defaultValues = metadataProvider.featuresAndSources()
      .map(FeatureMetadata::defaultValue)
      .toTypedArray()
    laboratory.setFeatures(*defaultValues)
  }

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
