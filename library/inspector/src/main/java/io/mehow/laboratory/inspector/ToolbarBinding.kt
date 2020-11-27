package io.mehow.laboratory.inspector

import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.mehow.laboratory.inspector.SearchMode.Active
import io.mehow.laboratory.inspector.SearchMode.Idle
import io.mehow.laboratory.inspector.SearchViewModel.Event.ToggleSearchMode
import io.mehow.laboratory.inspector.SearchViewModel.Event.UpdateQuery
import io.mehow.laboratory.inspector.SearchViewModel.SearchUiModel

internal class ToolbarBinding(
  view: View,
  private val onSearchEventsListener: (SearchViewModel.Event) -> Unit,
  private val onResetEventsListener: () -> Unit,
) {
  private val toolbar = view.findViewById<MotionLayout>(R.id.io_mehow_laboratory_toolbar)
  private val searchQuery = view.findViewById<AppCompatEditText>(R.id.io_mehow_laboratory_feature_query)
  private val toggleSearch = view.findViewById<KyrieImageView>(R.id.io_mehow_laboratory_toggle_search)
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

    toolbar.setTransition(R.id.io_mehow_laboratory_search_transition)
    toggleSearch.setOnClickListener { onSearchEventsListener(ToggleSearchMode) }
    clearQuery.setOnClickListener { searchQuery.setText("") }
    resetFeatures.setOnClickListener { resetFeaturesDialog.show() }
  }

  fun render(uiModel: SearchUiModel) {
    uiModel.mode.applyTransition(toolbar)
    clearQuery.springVisibility(isVisible = uiModel.showSearch && uiModel.query.isNotEmpty())

    if (uiModel.showSearch) {
      searchQuery.focusAndShowKeyboard()
    } else {
      searchQuery.hideKeyboard()
      searchQuery.setText("")
    }
  }

  private val SearchMode.applyTransition
    get() = when (this) {
      Idle -> MotionLayout::transitionToStart
      Active -> MotionLayout::transitionToEnd
    }
}
