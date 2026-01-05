package io.mehow.laboratory.inspector

import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import io.mehow.laboratory.inspector.ToolbarViewModel.UiModel

internal class ToolbarBinding(toolbarView: View) {
  private val context = toolbarView.context

  private val titleView =
    toolbarView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_toolbar_title)
  private val queryView =
    toolbarView.findViewById<AppCompatEditText>(R.id.io_mehow_laboratory_feature_query)
  private val openSearchView =
    toolbarView.findViewById<ShapeableImageView>(R.id.io_mehow_laboratory_open_search)
  private val closeSearchView =
    toolbarView.findViewById<ShapeableImageView>(R.id.io_mehow_laboratory_close_search)
  private val clearQueryView =
    toolbarView.findViewById<ShapeableImageView>(R.id.io_mehow_laboratory_clear_query)
  private val resetFeaturesView =
    toolbarView.findViewById<ShapeableImageView>(R.id.io_mehow_laboratory_reset_features)

  private var uiModel: UiModel? = null

  init {
    var oldText: String? = null
    queryView.doAfterTextChanged { editable ->
      val query = editable?.toString()?.trim()
      if (query != null && oldText != query) {
        oldText = query
        uiModel?.updateQuery(query)
      }
    }
    openSearchView.setOnClickListener { uiModel?.openSearch() }
    closeSearchView.setOnClickListener { uiModel?.closeSearch() }
    clearQueryView.setOnClickListener { queryView.setText("") }
    resetFeaturesView.setOnClickListener { showResetDialog() }

    toolbarView.doOnApplyWindowInsets { view, insets, padding ->
      val insets = insets.getTopInsets()
      view.updatePadding(
        left = padding.left + insets.left,
        top = padding.top + insets.top,
        right = padding.right + insets.right,
      )
    }
  }

  fun render(uiModel: UiModel) {
    this.uiModel = uiModel
    closeSearchView.isVisible = uiModel.isSearchOpen
    queryView.isVisible = uiModel.isSearchOpen
    openSearchView.isGone = uiModel.isSearchOpen
    titleView.isGone = uiModel.isSearchOpen
    clearQueryView.isVisible = uiModel.isSearchOpen && uiModel.query.isNotEmpty()

    if (uiModel.isSearchOpen) {
      queryView.focusAndShowKeyboard()
    } else {
      queryView.hideKeyboard()
      queryView.setText("")
    }
  }

  private fun showResetDialog() {
    MaterialAlertDialogBuilder(context)
      .setTitle(R.string.io_mehow_laboratory_reset_title)
      .setMessage(R.string.io_mehow_laboratory_reset_message)
      .setNegativeButton(R.string.io_mehow_laboratory_cancel) { _, _ -> }
      .setPositiveButton(R.string.io_mehow_laboratory_reset) { _, _ ->
        uiModel?.resetFeatureFlags()
      }
      .create()
      .show()
  }
}
