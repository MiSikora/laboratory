package io.mehow.laboratory.inspector

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
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
  private var listener: OptionGroupListener? = null

  init {
    isSelectionRequired = true
  }

  fun setOnSelectFeatureListener(listener: OptionGroupListener?) {
    this.listener = listener
  }

  fun render(models: List<OptionUiModel>, isEnabled: Boolean) {
    children.filterIsInstance<Chip>().forEach(::removeOnCheckedChangeListener)
    removeAllViews()
    models.map { createChip(it, isEnabled) }.forEach(::addView)
  }

  private fun createChip(model: OptionUiModel, isEnabled: Boolean): Chip {
    val chip = inflater.inflate(R.layout.io_mehow_laboratory_feature_option_chip, this, false) as Chip
    return chip.apply {
      text = model.option.name
      isChecked = model.isSelected
      if (model.supervisedFeatures.isNotEmpty()) {
        chipIcon = ContextCompat.getDrawable(context, R.drawable.io_mehow_laboratory_supervisor)
        setOnLongClickListener { showSupervisedFeaturesMenu(this, model.supervisedFeatures) }
      }
      isActivated = isEnabled
      this.isEnabled = isEnabled
      setOnCheckedChangeListener(createListener(model))
    }
  }

  private fun createListener(model: OptionUiModel) = CompoundButton.OnCheckedChangeListener { chip, isChecked ->
    if (isChecked) {
      (chip as Chip).deselectOtherChips()
      listener?.onSelectOption(model.option)
    }
  }

  // ChipGroup.isSingleSelection does not work with initial selection from code.
  private fun Chip.deselectOtherChips() {
    children.filterIsInstance<Chip>()
        .filter { it !== this }
        .forEach { chip -> chip.isChecked = false }
  }

  private fun removeOnCheckedChangeListener(chip: Chip) = chip.setOnCheckedChangeListener(null)

  private fun showSupervisedFeaturesMenu(anchor: Chip, features: List<Class<Feature<*>>>): Boolean {
    PopupMenu(context, anchor).apply {
      features.forEachIndexed { index, feature ->
        menu.add(0, index, index, feature.simpleName)
      }
      setOnMenuItemClickListener {
        listener?.onSelectSupervisedFeature(features[it.order])
        true
      }
    }.show()
    return true
  }

  interface OptionGroupListener {
    fun onSelectOption(option: Feature<*>)

    fun onSelectSupervisedFeature(feature: Class<Feature<*>>)
  }
}
