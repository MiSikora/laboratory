package io.mehow.laboratory.inspector

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.OptionViewGroup.OptionGroupListener
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

    override fun areContentsTheSame(old: FeatureUiModel, new: FeatureUiModel) = old == new

    // Prevent item animation change.
    override fun getChangePayload(old: FeatureUiModel, new: FeatureUiModel) = Unit
  }

  interface Listener : OptionGroupListener, OnSelectSourceListener {
    override fun onSelectSource(option: Feature<*>) = onSelectOption(option)

    override fun onSelectSupervisedFeature(feature: Class<out Feature<*>>) = onGoToFeature(feature)

    fun onGoToFeature(feature: Class<out Feature<*>>)
  }
}
