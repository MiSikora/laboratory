package io.mehow.laboratory.inspector

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.laboratory.inspector.FeatureModelsView.OnSelectFeatureListener
import io.mehow.laboratory.inspector.FeatureSourcesView.OnSelectSourceListener

internal class FeatureGroupAdapter(
  private val listener: Listener,
) : ListAdapter<FeatureGroup, FeatureGroupViewHolder>(DiffCallback) {
  override fun getItemViewType(position: Int) = R.layout.io_mehow_laboratory_feature_group_item

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureGroupViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    return FeatureGroupViewHolder(view, listener)
  }

  override fun onBindViewHolder(holder: FeatureGroupViewHolder, position: Int) = holder.bind(getItem(position))

  private object DiffCallback : ItemCallback<FeatureGroup>() {
    override fun areItemsTheSame(old: FeatureGroup, new: FeatureGroup) = old.fqcn == new.fqcn

    override fun areContentsTheSame(old: FeatureGroup, new: FeatureGroup): Boolean {
      return old.models.selected == new.models.selected && old.sources.selected == new.sources.selected
    }

    // Prevent item animation change.
    override fun getChangePayload(old: FeatureGroup, new: FeatureGroup) = Unit

    private val List<FeatureModel>.selected get() = firstOrNull(FeatureModel::isSelected)
  }

  interface Listener : OnSelectFeatureListener, OnSelectSourceListener
}
