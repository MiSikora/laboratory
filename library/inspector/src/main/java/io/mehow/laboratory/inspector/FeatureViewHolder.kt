package io.mehow.laboratory.inspector

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.textview.MaterialTextView

internal class FeatureViewHolder(
  itemView: View,
  listener: FeatureAdapter.Listener,
) : ViewHolder(itemView) {
  private val name = itemView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_feature_name)
  private val description = itemView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_feature_description)
  private val sources = itemView.findViewById<SourceViewGroup>(R.id.io_mehow_laboratory_feature_sources)
  private val divider = itemView.findViewById<View>(R.id.io_mehow_laboratory_sources_divider)
  private val options = itemView.findViewById<OptionViewGroup>(R.id.io_mehow_laboratory_feature_options)

  init {
    sources.setOnSelectSourceListener(listener)
    options.setOnSelectFeatureListener(listener)
  }

  fun bind(group: FeatureUiModel) {
    name.text = group.name
    description.text = group.description
    description.isVisible = group.description.isNotBlank()
    options.render(group.models, group.isCurrentSourceLocal)
    sources.render(group.sources)
    sources.isVisible = group.hasMultipleSources
    divider.isVisible = group.hasMultipleSources
  }
}
