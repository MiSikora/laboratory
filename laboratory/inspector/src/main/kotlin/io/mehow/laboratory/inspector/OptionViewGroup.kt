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
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = MaterialR.attr.chipGroupStyle,
) : ChipGroup(context, attrs, defStyle) {
  init {
    isSelectionRequired = true
  }

  private val inflater = LayoutInflater.from(context)
  private var listener: OptionGroupListener? = null
  private var options = emptyList<Feature<*>>()
  private var selectedOption: Feature<*>? = null
  private var isSelectionEnabled = false

  fun setOnSelectFeatureListener(listener: OptionGroupListener?) {
    this.listener = listener
  }

  fun setOptions(options: List<Feature<*>>) {
    this.options = options
    chips.forEach(::removeOnCheckedChangeListener)
    removeAllViews()
    val selectedOption = selectedOption ?: options.firstOrNull()?.defaultOption
    options.forEach { option -> addChip(option, option == selectedOption, isSelectionEnabled) }
  }

  fun setSelectedOption(option: Feature<*>) {
    selectedOption = option
    chips.forEach { chip -> chip.isChecked = chip.tag == option }
  }

  fun setSelectionEnabled(enable: Boolean) {
    this.isSelectionEnabled = enable
    chips.forEach { chip ->
      chip.isActivated = enable
      chip.isEnabled = enable
    }
  }

  private fun addChip(option: Feature<*>, select: Boolean, enable: Boolean) {
    val chip =
      inflater.inflate(R.layout.io_mehow_laboratory_feature_option_chip, this, false) as Chip
    chip.apply {
      tag = option
      text = option.name
      isChecked = select
      isActivated = enable
      isEnabled = enable
      setOnCheckedChangeListener(createListener(option))
    }
    addView(chip)
  }

  private fun createListener(option: Feature<*>) =
    CompoundButton.OnCheckedChangeListener { chip, isChecked ->
      if (isChecked) {
        (chip as Chip).deselectOtherChips()
        listener?.onSelectOption(option)
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
