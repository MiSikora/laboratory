package io.mehow.laboratory.inspector

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.mehow.laboratory.Feature
import com.google.android.material.R as MaterialR

internal class OptionViewGroup @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet,
  defStyle: Int = MaterialR.attr.chipGroupStyle,
) : ChipGroup(context, attrs, defStyle) {
  private val inflater = LayoutInflater.from(context)
  private var listener: OnSelectFeatureListener? = null

  init {
    isSelectionRequired = true
  }

  fun setOnSelectFeatureListener(listener: OnSelectFeatureListener?) {
    this.listener = listener
  }

  fun render(models: List<OptionUiModel>, isCurrentSourceLocal: Boolean) {
    children.filterIsInstance<Chip>().forEach(::removeOnCheckedChangeListener)
    removeAllViews()
    models.map { createChip(it, isCurrentSourceLocal) }.forEach(::addView)
  }

  private fun createChip(model: OptionUiModel, isCurrentSourceLocal: Boolean): Chip {
    val chip = inflater.inflate(R.layout.io_mehow_laboratory_feature_option_chip, this, false) as Chip
    return chip.apply {
      text = model.option.name
      isChecked = model.isSelected
      isActivated = isCurrentSourceLocal
      isEnabled = isCurrentSourceLocal
      setOnCheckedChangeListener(createListener(model))
    }
  }

  private fun createListener(model: OptionUiModel) = CompoundButton.OnCheckedChangeListener { chip, isChecked ->
    if (isChecked) {
      (chip as Chip).deselectOtherChips()
      listener?.onSelectFeature(model.option)
    }
  }

  // ChipGroup.isSingleSelection does not work with initial selection from code.
  private fun Chip.deselectOtherChips() {
    children.filterIsInstance<Chip>()
        .filter { it !== this }
        .forEach { chip -> chip.isChecked = false }
  }

  private fun removeOnCheckedChangeListener(chip: Chip) = chip.setOnCheckedChangeListener(null)

  interface OnSelectFeatureListener {
    fun onSelectFeature(feature: Feature<*>)
  }
}
