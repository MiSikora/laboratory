package io.mehow.laboratory.inspector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.android.material.textview.MaterialTextView
import io.mehow.laboratory.Feature

internal class FeatureSourceAdapter(
  private val features: List<Feature<*>>,
) : BaseAdapter() {
  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val inflater = LayoutInflater.from(parent.context)
    val viewHolder = if (convertView == null) {
      val view = inflater.inflate(R.layout.io_mehow_laboratory_feature_source_spinner_item, parent, false)
      FeatureSourceViewHolder(view as MaterialTextView).apply { view.tag = this }
    } else {
      convertView.tag as FeatureSourceViewHolder
    }
    viewHolder.bind(getItem(position))
    return viewHolder.item
  }

  override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
    val inflater = LayoutInflater.from(parent.context)
    val viewHolder = if (convertView == null) {
      val view = inflater.inflate(R.layout.io_mehow_laboratory_feature_source_drop_down_item, parent, false)
      FeatureSourceViewHolder(view as MaterialTextView).apply { view.tag = this }
    } else {
      convertView.tag as FeatureSourceViewHolder
    }
    viewHolder.bind(getItem(position))
    return viewHolder.item
  }

  override fun getCount() = features.size

  override fun getItem(position: Int) = features[position]

  override fun getItemId(position: Int) = position.toLong()

  fun positionOf(feature: Feature<*>): Int = features.indexOf(feature)
}
