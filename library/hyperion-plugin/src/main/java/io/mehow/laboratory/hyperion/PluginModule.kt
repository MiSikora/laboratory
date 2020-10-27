package io.mehow.laboratory.hyperion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.mehow.laboratory.inspector.LaboratoryActivity
import com.willowtreeapps.hyperion.plugin.v1.PluginModule as HyperionPluginModule

internal class PluginModule : HyperionPluginModule() {
  override fun getName(): Int = R.string.io_mehow_laboratory_plugin_id

  override fun createPluginView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
    return layoutInflater.inflate(R.layout.io_mehow_laboratory_plugin_item, parent, false).apply {
      setOnClickListener { LaboratoryActivity.start(context) }
    }
  }
}
