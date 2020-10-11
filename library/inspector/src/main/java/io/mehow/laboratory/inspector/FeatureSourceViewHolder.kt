package io.mehow.laboratory.inspector

import com.google.android.material.textview.MaterialTextView
import io.mehow.laboratory.Feature

internal class FeatureSourceViewHolder(val item: MaterialTextView) {
  fun bind(feature: Feature<*>) {
    item.text = feature.name
  }
}
