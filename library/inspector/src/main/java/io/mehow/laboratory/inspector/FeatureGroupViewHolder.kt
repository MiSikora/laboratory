package io.mehow.laboratory.inspector

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.textview.MaterialTextView

internal class FeatureGroupViewHolder(
  itemView: View,
  listener: FeatureGroupAdapter.Listener,
) : ViewHolder(itemView) {
  private val featureName = itemView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_feature_name)
  private val featureDescription = itemView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_feature_description)
  private val featureSources = itemView.findViewById<FeatureSourcesView>(R.id.io_mehow_laboratory_feature_sources)
  private val sourcesDivider = itemView.findViewById<View>(R.id.io_mehow_laboratory_sources_divider)
  private val featureValues = itemView.findViewById<FeatureModelsView>(R.id.io_mehow_laboratory_feature_values)

  init {
    featureSources.setOnSelectSourceListener(listener)
    featureValues.setOnSelectFeatureListener(listener)
  }

  fun bind(group: FeatureGroup) {
    featureName.text = group.name
    featureDescription.text = group.description
    featureDescription.isVisible = group.description.isNotBlank()
    featureValues.render(group.models, group.isCurrentSourceLocal)
    featureSources.render(group.sources)
    featureSources.isVisible = group.hasMultipleSources
    sourcesDivider.isVisible = group.hasMultipleSources
  }
}
