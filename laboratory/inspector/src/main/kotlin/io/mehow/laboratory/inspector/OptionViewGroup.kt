package io.mehow.laboratory.inspector

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import com.google.android.material.R as MaterialR
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.mehow.laboratory.Feature

internal class OptionViewGroup
@JvmOverloads
constructor(context: Context, attrs: AttributeSet, defStyle: Int = MaterialR.attr.chipGroupStyle) :
  ChipGroup(context, attrs, defStyle) {
  private val inflater = LayoutInflater.from(context)
  private var listener: OptionGroupListener? = null

  init {
    isSelectionRequired = true
  }

  fun setOnSelectFeatureListener(listener: OptionGroupListener?) {
    this.listener = listener
  }

  fun render(models: List<OptionUiModel>, isEnabled: Boolean) {
    chips.forEach(::removeOnCheckedChangeListener)
    removeAllViews()
    models.map { createChip(it, isEnabled) }.forEach(::addView)
  }

  private fun createChip(model: OptionUiModel, isEnabled: Boolean): Chip {
    val chip =
      inflater.inflate(R.layout.io_mehow_laboratory_feature_option_chip, this, false) as Chip
    return chip.apply {
      text = model.option.name
      isChecked = model.isSelected
      isActivated = isEnabled
      this.isEnabled = isEnabled
      setOnCheckedChangeListener(createListener(model))
    }
  }

  private fun createListener(model: OptionUiModel) =
    CompoundButton.OnCheckedChangeListener { chip, isChecked ->
      if (isChecked) {
        (chip as Chip).deselectOtherChips()
        listener?.onSelectOption(model.option)
      }
    }

  // ChipGroup.isSingleSelection does not work with initial selection from code.
  private fun Chip.deselectOtherChips() {
    chips.filter { it !== this }.forEach { chip -> chip.isChecked = false }
  }

  private fun removeOnCheckedChangeListener(chip: Chip) = chip.setOnCheckedChangeListener(null)

  private val chips: Sequence<Chip>
    get() = sequence {
      for (index in 0 until childCount) {
        val chip = getChildAt(index) as? Chip ?: continue
        yield(chip)
      }
    }

  interface OptionGroupListener {
    fun onSelectOption(option: Feature<*>)
  }
}
