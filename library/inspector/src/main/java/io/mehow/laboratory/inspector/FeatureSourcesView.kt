package io.mehow.laboratory.inspector

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import io.mehow.laboratory.Feature
import androidx.appcompat.R as AppCompatR

internal class FeatureSourcesView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet,
  defStyle: Int = AppCompatR.attr.spinnerStyle,
) : AppCompatSpinner(context, attrs, defStyle) {
  internal var listener: OnSelectSourceListener? = null

  override fun getAdapter() = super.getAdapter() as? FeatureSourceAdapter

  fun setOnSelectSourceListener(listener: OnSelectSourceListener?) {
    this.listener = listener
  }

  fun render(models: List<FeatureModel>) {
    val features = models.map(FeatureModel::feature)
    val selectedFeature = models.firstOrNull(FeatureModel::isSelected)?.feature ?: return
    val newAdapter = FeatureSourceAdapter(features)
    onItemSelectedListener = createListener()
    adapter = newAdapter
    val position = newAdapter.positionOf(selectedFeature)
    setSelection(position)
  }

  private fun createListener() = object : OnItemSelectedListener {
    var ignoreItem = true

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
      if (ignoreItem) {
        ignoreItem = false
        return
      }
      val item = requireNotNull(adapter) {
        "Feature source adapter is not set"
      }.getItem(position)
      listener?.onSelectSource(item)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
  }

  interface OnSelectSourceListener {
    fun onSelectSource(feature: Feature<*>)
  }
}
