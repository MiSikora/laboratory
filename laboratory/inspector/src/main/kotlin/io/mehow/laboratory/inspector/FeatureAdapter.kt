package io.mehow.laboratory.inspector

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.laboratory.Feature
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.OptionViewGroup.OptionGroupListener
import io.mehow.laboratory.options
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class FeatureAdapter(
  private val laboratory: Laboratory,
  private val lifecycle: Lifecycle,
  private val listener: Listener,
) : ListAdapter<FeatureMetadata, FeatureViewHolder>(DiffCallback) {
  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int) = getItem(position).id

  override fun getItemViewType(position: Int) = R.layout.io_mehow_laboratory_feature_item

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    return FeatureViewHolder(
      itemView = view,
      listener = listener,
      isRemoteSourceAvailable = laboratory.remoteStorage() != null,
      lifecycle = lifecycle,
      selectedOptionFlow = laboratory::observe,
      selectedSourceFlow = laboratory::observeSource,
    )
  }

  override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) =
    holder.bind(getItem(position))

  override fun onViewRecycled(holder: FeatureViewHolder) = holder.cancelJobs()

  private object DiffCallback : ItemCallback<FeatureMetadata>() {
    override fun areItemsTheSame(old: FeatureMetadata, new: FeatureMetadata) = old.id == new.id

    override fun areContentsTheSame(old: FeatureMetadata, new: FeatureMetadata) = old == new

    // Prevent item animation change.
    override fun getChangePayload(old: FeatureMetadata, new: FeatureMetadata) = Unit
  }

  interface Listener : OptionGroupListener {
    fun onSelectSource(feature: Class<out Feature<*>>, source: Feature.Source)
  }
}

private fun <T : Feature<out T>> Laboratory.observeSource(
  feature: Class<out T>
): Flow<Feature.Source> {
  val defaultSource = feature.options[0].defaultSource
  return localStorage().observeSource(feature).map { source -> source ?: defaultSource }
}
