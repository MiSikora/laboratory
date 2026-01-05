package io.mehow.laboratory.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ToolbarViewModel(private val laboratory: Laboratory, val scope: CoroutineScope) :
  ViewModel() {
  private val _uiModels =
    MutableStateFlow(
      UiModel(
        isSearchOpen = false,
        query = QueryString.create(""),
        openSearch = ::openSearch,
        closeSearch = ::closeSearch,
        updateQuery = ::updateQuery,
        resetFeatureFlags = ::resetFeatureFlags,
      )
    )

  val uiModels: StateFlow<UiModel>
    get() = _uiModels

  @OptIn(FlowPreview::class)
  val searchQueries =
    _uiModels
      .map { model -> model.query }
      .distinctUntilChanged()
      .debounce { query -> if (query.isEmpty()) 0 else 200 }
      .stateIn(scope, SharingStarted.Eagerly, QueryString.Empty)

  private fun openSearch() {
    _uiModels.update { model -> model.copy(isSearchOpen = true, query = QueryString.Empty) }
  }

  private fun closeSearch() {
    _uiModels.update { model -> model.copy(isSearchOpen = false, query = QueryString.Empty) }
  }

  private fun updateQuery(text: String) {
    _uiModels.update { model ->
      if (model.isSearchOpen) {
        model.copy(query = QueryString.create(text))
      } else {
        model
      }
    }
  }

  private fun resetFeatureFlags() {
    scope.launch { laboratory.clear() }
  }

  override fun onCleared() {
    scope.cancel()
  }

  class Factory(private val configuration: LaboratoryActivity.Configuration) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      require(modelClass == ToolbarViewModel::class.java) { "Cannot create $modelClass" }
      @Suppress("UNCHECKED_CAST")
      return ToolbarViewModel(
        configuration.laboratory,
        CoroutineScope(Dispatchers.Main.immediate + SupervisorJob()),
      )
        as T
    }
  }

  data class UiModel(
    val isSearchOpen: Boolean,
    val query: QueryString,
    val openSearch: () -> Unit,
    val closeSearch: () -> Unit,
    val updateQuery: (String) -> Unit,
    val resetFeatureFlags: () -> Unit,
  )
}
