package io.mehow.laboratory.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.description
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration
import io.mehow.laboratory.source
import io.mehow.laboratory.supervisorOption
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class GroupViewModel(
  private val laboratory: Laboratory,
  private val groupFeatureFactory: FeatureFactory,
  deprecationHandler: DeprecationHandler,
  searchQueries: Flow<SearchQuery>,
) : ViewModel() {
  private val featureMetadataFactory = FeatureMetadata.Factory(deprecationHandler)

  fun selectFeature(feature: Feature<*>) {
    viewModelScope.launch(start = UNDISPATCHED) { laboratory.setOption(feature) }
  }

  private val initiatedSearchQueries = flow {
    emit(SearchQuery.Empty)
    emitAll(searchQueries)
  }.distinctUntilChanged()

  private val featureGroups = flow {
    val groups = withContext(Dispatchers.Default) {
      groupFeatureFactory.create()
          .mapNotNull(featureMetadataFactory::create)
          .filter { it.deprecationPhenotype != DeprecationPhenotype.Hide }
          .map { it.observeGroup(laboratory) }
          .combineLatest()
    }
    val searchedGroups = combine(groups, initiatedSearchQueries) { group, query -> group.search(query) }
        .map { it.sortedWith(FeatureUiModel.NaturalComparator) }
        .flowOn(Dispatchers.Default)
    emitAll(searchedGroups)
  }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

  fun observeFeatureGroup(): Flow<List<FeatureUiModel>> = featureGroups

  class Factory(
    private val configuration: Configuration,
    private val sectionName: String,
    private val searchQueries: Flow<SearchQuery>,
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      require(modelClass == GroupViewModel::class.java) { "Cannot create $modelClass" }
      @Suppress("UNCHECKED_CAST")
      return GroupViewModel(
          configuration.laboratory,
          configuration.factory(sectionName),
          configuration.deprecation,
          searchQueries
      ) as T
    }
  }

  private class FeatureMetadata(
    private val feature: Class<Feature<*>>,
    private val deprecationHandler: DeprecationHandler,
  ) {
    private val simpleReadableName = feature.name.substringAfterLast('.').replace('$', '.')

    private val options = feature.enumConstants!!.toList<Feature<*>>()

    private val sourceMetadata = feature.source?.let { FeatureMetadata(it, deprecationHandler) }

    private val deprecationLevel = feature.annotations
        .filterIsInstance<Deprecated>()
        .firstOrNull()
        ?.level

    val deprecationPhenotype = deprecationLevel?.let(deprecationHandler::getPhenotype)

    private val deprecationPlacement = deprecationLevel?.let(deprecationHandler::getAlignment)

    fun observeGroup(laboratory: Laboratory): Flow<FeatureUiModel> {
      val featureEmissions = observeOptions(laboratory)
      val sourceEmissions = sourceMetadata?.observeOptions(laboratory) ?: flowOf(emptyList())
      val supervisorEmissions = feature.supervisorOption
          ?.let { laboratory.observe(it::class.java) }
          ?: flowOf(null)
      return combine(featureEmissions, sourceEmissions, supervisorEmissions) { features, sources, supervisor ->
        FeatureUiModel(
            type = feature,
            name = simpleReadableName,
            description = feature.description.tokenize(),
            models = features,
            sources = sources,
            deprecationAlignment = deprecationPlacement,
            deprecationPhenotype = deprecationPhenotype,
            supervisorOption = supervisor,
        )
      }
    }

    private fun observeOptions(laboratory: Laboratory) = laboratory.observe(feature).map { selectedFeature ->
      options.map { option -> OptionUiModel(option, isSelected = selectedFeature == option) }
    }

    class Factory(private val deprecationHandler: DeprecationHandler) {
      fun create(feature: Class<Feature<*>>) = feature
          .takeUnless { it.enumConstants.isNullOrEmpty() }
          ?.let { FeatureMetadata(it, deprecationHandler) }
    }
  }
}
