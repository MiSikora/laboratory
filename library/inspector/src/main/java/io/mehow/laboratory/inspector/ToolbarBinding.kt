package io.mehow.laboratory.inspector

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import io.mehow.laboratory.inspector.SearchViewModel.Event.CloseSearch
import io.mehow.laboratory.inspector.SearchViewModel.Event.OpenSearch
import io.mehow.laboratory.inspector.SearchViewModel.Event.UpdateQuery
import io.mehow.laboratory.inspector.SearchViewModel.UiModel

internal class ToolbarBinding(
  view: View,
  private val onSearchEventsListener: (SearchViewModel.Event) -> Unit,
  private val onResetEventsListener: () -> Unit,
) {
  private val title = view.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_title)
  private val searchQuery = view.findViewById<AppCompatEditText>(R.id.io_mehow_laboratory_feature_query)
  private val openSearch = view.findViewById<ShapeableImageView>(R.id.io_mehow_laboratory_open_search)
  private val closeSearch = view.findViewById<ShapeableImageView>(R.id.io_mehow_laboratory_close_search)
  private val clearQuery = view.findViewById<ShapeableImageView>(R.id.io_mehow_laboratory_clear_query)
  private val resetFeatures = view.findViewById<ShapeableImageView>(R.id.io_mehow_laboratory_reset_features)

  private val resetFeaturesDialog = MaterialAlertDialogBuilder(view.context)
      .setTitle(R.string.io_mehow_laboratory_reset_title)
      .setMessage(R.string.io_mehow_laboratory_reset_message)
      .setNegativeButton(R.string.io_mehow_laboratory_cancel) { _, _ -> }
      .setPositiveButton(R.string.io_mehow_laboratory_reset) { _, _ -> onResetEventsListener() }
      .create()

  init {
    var oldText: String? = null
    searchQuery.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(editable: Editable?) {
        val query = editable?.toString()
        if (query != null && oldText != query) {
          oldText = query
          onSearchEventsListener(UpdateQuery(query))
        }
      }
      override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) = Unit
    })
    openSearch.setOnClickListener { onSearchEventsListener(OpenSearch) }
    closeSearch.setOnClickListener { onSearchEventsListener(CloseSearch) }
    clearQuery.setOnClickListener { searchQuery.setText("") }
    resetFeatures.setOnClickListener { resetFeaturesDialog.show() }
  }

  fun render(uiModel: UiModel) {
    closeSearch.isVisible = uiModel.showSearch
    searchQuery.isVisible = uiModel.showSearch
    openSearch.isGone = uiModel.showSearch
    title.isGone = uiModel.showSearch
    clearQuery.isVisible = uiModel.showSearch && uiModel.query.isNotEmpty()

    if (uiModel.showSearch) {
      searchQuery.focusAndShowKeyboard()
    } else {
      searchQuery.hideKeyboard()
      searchQuery.setText("")
    }
  }
}
