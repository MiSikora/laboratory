package io.mehow.laboratory.inspector

import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import io.mehow.laboratory.inspector.SearchViewModel.Event.ToggleSearchMode
import io.mehow.laboratory.inspector.SearchViewModel.Event.UpdateQuery
import io.mehow.laboratory.inspector.SearchViewModel.SearchUiModel

internal class ToolbarBinding(
  view: View,
  private val onSearchEventsListener: (SearchViewModel.Event) -> Unit,
  private val onResetEventsListener: () -> Unit,
) {
  private val title = view.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_title)
  private val searchQuery = view.findViewById<AppCompatEditText>(R.id.io_mehow_laboratory_feature_query)
  private val closeSearch = view.findViewById<AppCompatImageView>(R.id.io_mehow_laboratory_close_search)
  private val showSearch = view.findViewById<AppCompatImageView>(R.id.io_mehow_laboratory_show_search)
  private val clearQuery = view.findViewById<AppCompatImageView>(R.id.io_mehow_laboratory_clear_query)
  private val resetFeatures = view.findViewById<AppCompatImageView>(R.id.io_mehow_laboratory_reset_features)

  private val resetFeaturesDialog = MaterialAlertDialogBuilder(view.context)
      .setTitle(R.string.io_mehow_laboratory_reset_title)
      .setMessage(R.string.io_mehow_laboratory_reset_message)
      .setNegativeButton(R.string.io_mehow_laboratory_cancel) { _, _ -> }
      .setPositiveButton(R.string.io_mehow_laboratory_reset) { _, _ -> onResetEventsListener() }
      .create()

  init {
    var oldText: String? = null
    searchQuery.addTextChangedListener { editable ->
      val query = editable?.toString() ?: return@addTextChangedListener
      if (oldText == query) return@addTextChangedListener
      oldText = query
      onSearchEventsListener(UpdateQuery(query))
    }
    closeSearch.setOnClickListener { onSearchEventsListener(ToggleSearchMode) }
    showSearch.setOnClickListener { onSearchEventsListener(ToggleSearchMode) }
    clearQuery.setOnClickListener { searchQuery.setText("") }
    resetFeatures.setOnClickListener { resetFeaturesDialog.show() }
  }

  fun render(uiModel: SearchUiModel) {
    title.isVisible = !uiModel.showSearch
    showSearch.isVisible = !uiModel.showSearch

    closeSearch.isVisible = uiModel.showSearch
    searchQuery.isVisible = uiModel.showSearch
    clearQuery.isVisible = uiModel.showSearch && uiModel.query.isNotEmpty()

    if (uiModel.showSearch) {
      searchQuery.focusAndShowKeyboard()
    } else {
      searchQuery.setText("")
      searchQuery.hideKeyboard()
    }
  }
}
