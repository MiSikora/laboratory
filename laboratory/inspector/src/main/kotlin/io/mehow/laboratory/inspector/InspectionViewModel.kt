package io.mehow.laboratory.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mehow.laboratory.Feature
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class InspectionViewModel(
  private val laboratory: Laboratory,
  private val searchQueries: StateFlow<QueryString>,
  private val loaders: Map<String, FeatureMetadata.Loader>,
  private val scope: CoroutineScope,
  private val dispatcher: CoroutineContext,
) : ViewModel() {
  private val featureFlows = mutableMapOf<String, Flow<List<FeatureMetadata>>>()

  fun sectionFlow(name: String) =
    featureFlows.getOrPut(name) {
      flow {
          val metadata = loaders[name]?.load().orEmpty()
          emitAll(searchQueries.map(metadata::matches))
        }
        .flowOn(dispatcher)
        .stateIn(scope, SharingStarted.Eagerly, initialValue = emptyList())
    }

  fun selectFeature(feature: Feature<*>) {
    scope.launch { laboratory.setOption(feature) }
  }

  fun selectSource(feature: Class<Feature<*>>, source: Feature.Source) {
    scope.launch {
      val storage = laboratory.localStorage()
      when (source) {
        Feature.Source.Local -> storage.setLocalSource(feature)
        Feature.Source.Remote -> storage.setRemoteSource(feature)
      }
    }
  }

  override fun onCleared() {
    scope.cancel()
  }

  class Factory(
    private val configuration: Configuration,
    private val searchQuery: StateFlow<QueryString>,
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      require(modelClass == InspectionViewModel::class.java) { "Cannot create $modelClass" }
      @Suppress("UNCHECKED_CAST")
      return InspectionViewModel(
        configuration.laboratory,
        searchQuery,
        configuration.metadataLoaders,
        CoroutineScope(Dispatchers.Main.immediate + SupervisorJob()),
        Dispatchers.Default,
      )
        as T
    }
  }
}
