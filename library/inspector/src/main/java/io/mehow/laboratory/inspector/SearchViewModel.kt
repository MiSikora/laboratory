package io.mehow.laboratory.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.mehow.laboratory.inspector.SearchMode.Active
import io.mehow.laboratory.inspector.SearchMode.Idle
import io.mehow.laboratory.inspector.SearchViewModel.Event.ToggleSearchMode
import io.mehow.laboratory.inspector.SearchViewModel.Event.UpdateQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

internal class SearchViewModel : ViewModel() {
  private val uiModelChanges = MutableSharedFlow<(SearchUiModel) -> SearchUiModel>()

  private val sharedUiModels = uiModelChanges.scan(
      initial = SearchUiModel(Idle, SearchQuery.Empty),
      operation = { currentModel, updateModel -> updateModel(currentModel) }
  ).shareIn(viewModelScope, SharingStarted.Lazily, replay = 1).distinctUntilChanged()

  val uiModels: Flow<SearchUiModel> = sharedUiModels

  fun sendEvent(event: Event) = when (event) {
    is ToggleSearchMode -> updateToggleMode()
    is UpdateQuery -> updateQuery(event)
  }

  private fun updateToggleMode() = viewModelScope.launch {
    uiModelChanges.emit { model ->
      model.copy(mode = model.mode.toggle(), query = SearchQuery.Empty)
    }
  }

  private fun updateQuery(event: UpdateQuery) = viewModelScope.launch {
    uiModelChanges.emit { model ->
      when (model.mode) {
        Idle -> model
        Active -> model.copy(query = SearchQuery(event.query))
      }
    }
  }

  object Factory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      require(modelClass == SearchViewModel::class.java) { "Cannot create $modelClass" }
      @Suppress("UNCHECKED_CAST")
      return SearchViewModel() as T
    }
  }

  sealed class Event {
    object ToggleSearchMode : Event()
    class UpdateQuery(val query: String) : Event()
  }

  data class SearchUiModel(
    val mode: SearchMode,
    val query: SearchQuery,
  ) {
    val showSearch = mode == Active
  }
}
