package io.mehow.laboratory.inspector

import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.textview.MaterialTextView
import io.mehow.laboratory.inspector.DeprecationPhenotype.Hide
import io.mehow.laboratory.inspector.DeprecationPhenotype.Show
import io.mehow.laboratory.inspector.DeprecationPhenotype.Strikethrough

internal class FeatureViewHolder(
  itemView: View,
  listener: FeatureAdapter.Listener,
) : ViewHolder(itemView) {
  private val name = itemView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_feature_name)
  private val supervisor = itemView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_feature_supervisor)
  private val description = itemView.findViewById<MaterialTextView>(R.id.io_mehow_laboratory_feature_description)
  private val sources = itemView.findViewById<SourceViewGroup>(R.id.io_mehow_laboratory_feature_sources)
  private val divider = itemView.findViewById<View>(R.id.io_mehow_laboratory_sources_divider)
  private val options = itemView.findViewById<OptionViewGroup>(R.id.io_mehow_laboratory_feature_options)

  init {
    sources.setOnSelectSourceListener(listener)
    options.setOnSelectFeatureListener(listener)
    description.movementMethod = LinkMovementMethod.getInstance()
  }

  fun bind(uiModel: FeatureUiModel) {
    name.text = uiModel.name
    name.paintFlags = when (uiModel.deprecationPhenotype) {
      null, Show -> name.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
      Strikethrough -> name.paintFlags or STRIKE_THRU_TEXT_FLAG
      Hide -> name.paintFlags
    }
    supervisor.isVisible = uiModel.supervisorName != null
    supervisor.text = uiModel.supervisorName?.let { supervisorName ->
      itemView.context.getString(R.string.io_mehow_laboratory_feature_supervisor, supervisorName)
    }
    description.setTextTokens(uiModel.description)
    description.isVisible = uiModel.description.isNotEmpty()
    options.render(uiModel.models, uiModel.isEnabled)
    sources.render(uiModel.sources)
    sources.isVisible = uiModel.hasMultipleSources
    divider.isVisible = uiModel.hasMultipleSources
  }
}
