package io.mehow.laboratory.inspector

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.laboratory.inspector.OptionViewGroup.OnSelectFeatureListener
import io.mehow.laboratory.inspector.SourceViewGroup.OnSelectSourceListener

internal class FeatureAdapter(
  private val listener: Listener,
) : ListAdapter<FeatureUiModel, FeatureViewHolder>(DiffCallback) {
  override fun getItemViewType(position: Int) = R.layout.io_mehow_laboratory_feature_item

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    return FeatureViewHolder(view, listener)
  }

  override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) = holder.bind(getItem(position))

  private object DiffCallback : ItemCallback<FeatureUiModel>() {
    override fun areItemsTheSame(old: FeatureUiModel, new: FeatureUiModel) = old.type == new.type

    override fun areContentsTheSame(old: FeatureUiModel, new: FeatureUiModel): Boolean {
      return old.models.selected == new.models.selected && old.sources.selected == new.sources.selected
    }

    // Prevent item animation change.
    override fun getChangePayload(old: FeatureUiModel, new: FeatureUiModel) = Unit

    private val List<OptionUiModel>.selected get() = firstOrNull(OptionUiModel::isSelected)
  }

  interface Listener : OnSelectFeatureListener, OnSelectSourceListener
}
