package io.mehow.laboratory.inspector

import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.CompoundButton
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.FeatureStyle.Hide
import io.mehow.laboratory.inspector.FeatureStyle.Show
import io.mehow.laboratory.inspector.FeatureStyle.Strikethrough
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

internal class FeatureViewHolder(
  itemView: View,
  listener: FeatureAdapter.Listener,
  private val lifecycle: Lifecycle,
  private val isRemoteSourceAvailable: Boolean,
  private val selectedOptionFlow: (Class<out Feature<*>>) -> Flow<Feature<*>>,
  private val selectedSourceFlow: (Class<out Feature<*>>) -> Flow<Feature.Source>,
) : ViewHolder(itemView) {
  private val nameControl =
    itemView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_feature_name)
  private val descriptionControl =
    itemView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_feature_description)
  private val sourceControl =
    itemView.findViewById<SwitchMaterial>(R.id.io_mehow_laboratory_feature_source)
  private val optionsControl =
    itemView.findViewById<OptionViewGroup>(R.id.io_mehow_laboratory_feature_options)

  private val switchListener =
    CompoundButton.OnCheckedChangeListener { _, isChecked ->
      val source = if (isChecked) Feature.Source.Remote else Feature.Source.Local
      listener.onSelectSource(metadata!!.type, source)
    }

  init {
    descriptionControl.movementMethod = LinkMovementMethod.getInstance()
    sourceControl.setOnCheckedChangeListener(switchListener)
    optionsControl.setOnSelectFeatureListener(listener)
  }

  private var metadata: FeatureMetadata? = null
  private var sourceJob: Job? = null
  private var optionJob: Job? = null

  fun bind(metadata: FeatureMetadata) {
    this.metadata = metadata

    nameControl.text = metadata.name
    nameControl.paintFlags =
      when (metadata.style) {
        Hide,
        Show -> nameControl.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
        Strikethrough -> nameControl.paintFlags or STRIKE_THRU_TEXT_FLAG
      }
    descriptionControl.setTextTokens(metadata.description)
    descriptionControl.isVisible = metadata.description.isNotEmpty()
    sourceControl.isVisible = isRemoteSourceAvailable && metadata.isRemote
    optionsControl.setOptions(metadata.options)

    val previousOptionJob = optionJob
    optionJob =
      lifecycle.coroutineScope.launch {
        previousOptionJob?.cancelAndJoin()
        selectedOptionFlow(metadata.type).flowWithLifecycle(lifecycle).collect { option ->
          optionsControl.setSelectedOption(option)
        }
      }

    val previousSourceJob = sourceJob
    sourceJob =
      lifecycle.coroutineScope.launch {
        previousSourceJob?.cancelAndJoin()
        selectedSourceFlow(metadata.type).flowWithLifecycle(lifecycle).collect { source ->
          if (sourceControl.isChecked != source.isRemote) {
            sourceControl.setOnCheckedChangeListener(null)
            sourceControl.isChecked = source.isRemote
            sourceControl.jumpDrawablesToCurrentState()
            sourceControl.setOnCheckedChangeListener(switchListener)
          }
          optionsControl.setSelectionEnabled(!isRemoteSourceAvailable || source.isLocal)
        }
      }
  }

  fun cancelJobs() {
    sourceJob?.cancel()
    optionJob?.cancel()
  }
}
