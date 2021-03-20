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

  fun bind(group: FeatureUiModel) {
    name.text = group.name
    name.paintFlags = when (group.deprecationPhenotype) {
      null, Show -> name.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
      Strikethrough -> name.paintFlags or STRIKE_THRU_TEXT_FLAG
      Hide -> name.paintFlags
    }
    supervisor.isVisible = group.supervisorName != null
    supervisor.text = group.supervisorName?.let { supervisorName ->
      itemView.context.getString(R.string.io_mehow_laboratory_feature_supervisor, supervisorName)
    }
    description.setTextTokens(group.description)
    description.isVisible = group.description.isNotEmpty()
    options.render(group.models, group.isEnabled)
    sources.render(group.sources)
    sources.isVisible = group.hasMultipleSources
    divider.isVisible = group.hasMultipleSources
  }
}
