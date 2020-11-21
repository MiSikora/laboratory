package io.mehow.laboratory.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration
import io.mehow.laboratory.source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext

internal class GroupViewModel(
  private val laboratory: Laboratory,
  private val groupFeatureFactory: FeatureFactory,
) : ViewModel() {
  suspend fun selectFeature(feature: Feature<*>) = laboratory.setOption(feature)

  private val featureGroups = flow {
    val listGroupFlow = withContext(Dispatchers.Default) {
      groupFeatureFactory.create()
          .mapNotNull(FeatureMetadata::create)
          .map { it.observeGroup(laboratory) }
          .observeElements()
    }.flowOn(Dispatchers.Default).map { featureGroups ->
      featureGroups.sortedBy(FeatureUiModel::name)
    }
    emitAll(listGroupFlow)
  }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

  fun observeFeatureGroup(): Flow<List<FeatureUiModel>> = featureGroups

  class Factory(
    private val configuration: Configuration,
    private val sectionName: String,
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      require(modelClass == GroupViewModel::class.java) { "Cannot create $modelClass" }
      @Suppress("UNCHECKED_CAST")
      return GroupViewModel(configuration.laboratory, configuration.factory(sectionName)) as T
    }
  }

  private class FeatureMetadata private constructor(private val feature: Class<Feature<*>>) {
    val simpleReadableName = feature.name.substringAfterLast('.').replace('$', '.')

    val options = feature.enumConstants!!.toList<Feature<*>>()

    val sourceMetadata = feature.source?.let(FeatureMetadata::create)

    fun observeGroup(laboratory: Laboratory): Flow<FeatureUiModel> {
      val featureEmissions = observeModels(laboratory)
      val sourceEmissions = sourceMetadata?.observeModels(laboratory) ?: flowOf(emptyList())
      return featureEmissions.combine(sourceEmissions) { features, sources ->
        FeatureUiModel(simpleReadableName, feature.name, features, sources)
      }
    }

    fun observeModels(laboratory: Laboratory) = laboratory.observe(feature).map { selectedFeature ->
      options.map { option -> OptionUiModel(option, isSelected = selectedFeature == option) }
    }

    companion object {
      fun create(feature: Class<Feature<*>>) = feature
          .takeUnless { it.enumConstants.isNullOrEmpty() }
          ?.let(::FeatureMetadata)
    }
  }
}
