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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration

@Suppress("LongParameterList")
internal class InspectorViewModel(
  private val laboratory: Laboratory,
  private val searchQueries: Flow<SearchQuery>,
  featureFactories: Map<String, FeatureFactory>,
  deprecationHandler: DeprecationHandler,
  private val computationDispatcher: CoroutineDispatcher,
) : ViewModel() {
  private val metadataFactory = FeatureMetadata.Factory(deprecationHandler, featureFactories)

  private val initiatedSearchQueries = flow {
    emit(SearchQuery.Empty)
    emitAll(searchQueries)
  }.distinctUntilChanged()

  private val sectionFlows = featureFactories.mapValues { (_, featureFactory) ->
    flow {
      val groups = withContext(computationDispatcher) {
        featureFactory.create()
            .mapNotNull(metadataFactory::create)
            .map { it.observeGroup(laboratory) }
            .combineLatest()
      }
      val searchedGroups = combine(groups, initiatedSearchQueries) { group, query -> group.search(query) }
          .map { it.sortedWith(FeatureUiModel.NaturalComparator) }
          .flowOn(computationDispatcher)
      emitAll(searchedGroups)
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)
  }

  fun sectionFlow(sectionName: String) = sectionFlows[sectionName] ?: emptyFlow()

  fun selectFeature(feature: Feature<*>) {
    viewModelScope.launch(start = UNDISPATCHED) { laboratory.setOption(feature) }
  }

  private val mutableNavigationFlow = MutableSharedFlow<FeatureCoordinates>()

  val featureCoordinatesFlow: Flow<FeatureCoordinates> get() = mutableNavigationFlow

  suspend fun goTo(feature: Class<Feature<*>>) = sectionFlows.values.asFlow().withIndex()
      .mapNotNull { (sectionIndex, sectionFlow) ->
        val listIndex = sectionFlow.first().map(FeatureUiModel::type).indexOf(feature)
        if (listIndex == -1) null else FeatureCoordinates(sectionIndex, listIndex)
      }
      .firstOrNull()
      ?.also { mutableNavigationFlow.emit(it) }

  private class FeatureMetadata(
    private val feature: Class<Feature<*>>,
    private val allFeatures: List<Class<Feature<*>>>,
    private val deprecationHandler: DeprecationHandler,
  ) {
    private val simpleReadableName = feature.name.substringAfterLast('.').replace('$', '.')

    private val options = feature.enumConstants!!.toList<Feature<*>>()

    private val sourceMetadata = feature.source?.let { FeatureMetadata(it, allFeatures, deprecationHandler) }

    private val deprecationLevel = feature.annotations
        .filterIsInstance<Deprecated>()
        .firstOrNull()
        ?.level

    private val deprecationPhenotype = deprecationLevel?.let(deprecationHandler::getPhenotype)

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
      options.map { option ->
        val supervisedFeatures = allFeatures.filter { it.supervisorOption == option }
        OptionUiModel(option, isSelected = selectedFeature == option, supervisedFeatures.toList())
      }
    }

    class Factory(
      private val deprecationHandler: DeprecationHandler,
      private val featureFactories: Map<String, FeatureFactory>,
    ) {
      private val allFeatures by lazy {
        featureFactories.values
            .flatMap { it.create() }
            .filterNot { it.enumConstants.isNullOrEmpty() }
      }

      fun create(feature: Class<Feature<*>>) = feature
          .takeUnless { it.enumConstants.isNullOrEmpty() }
          ?.let { FeatureMetadata(it, allFeatures, deprecationHandler) }
          ?.takeIf { it.deprecationPhenotype != DeprecationPhenotype.Hide }
    }
  }

  class Factory(
    private val configuration: Configuration,
    private val searchViewModel: SearchViewModel,
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      require(modelClass == InspectorViewModel::class.java) { "Cannot create $modelClass" }
      @Suppress("UNCHECKED_CAST") @OptIn(FlowPreview::class)
      return InspectorViewModel(
          configuration.laboratory,
          searchViewModel.uiModels.debounce(Duration.milliseconds(200)).map { it.query },
          configuration.featureFactories,
          configuration.deprecation,
          Dispatchers.Default,
      ) as T
    }
  }

  companion object
}
