package io.mehow.laboratory.inspector

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

@SuppressLint("ViewConstructor") // Created only from code.
internal class FeatureGroupView constructor(
  context: Context,
  featureGroup: FeatureGroup,
  private val onCheckFeatureChipListener: (Enum<*>) -> Unit
) : ChipGroup(context) {
  private val inflater = LayoutInflater.from(context)
  private val spacing = resources.getDimensionPixelSize(R.dimen.io_mehow_laboratory_spacing)

  init {
    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
      setMargins(spacing, spacing, spacing, 0)
    }
    isSelectionRequired = true
    featureGroup.models.map(::createChip).forEach(::addView)
  }

  private fun createChip(model: FeatureModel): Chip {
    val chip = inflater.inflate(R.layout.io_mehow_laboratory_feature_chip, this, false) as Chip
    return chip.apply {
      text = model.feature.name
      isChecked = model.isSelected
      setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
          deselectOtherChips()
          onCheckFeatureChipListener(model.feature)
        }
      }
    }
  }

  // ChipGroup.isSingleSelection does not work with initial selection from code.
  private fun Chip.deselectOtherChips() {
    children.filterIsInstance<Chip>()
      .filter { it !== this }
      .forEach { chip -> chip.isChecked = false }
  }
}
