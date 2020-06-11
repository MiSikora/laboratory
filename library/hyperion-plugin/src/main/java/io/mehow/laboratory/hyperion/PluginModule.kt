package io.mehow.laboratory.hyperion

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.mehow.laboratory.inspector.LaboratoryActivity
import com.willowtreeapps.hyperion.plugin.v1.PluginModule as HyperionPluginModule

internal class PluginModule : HyperionPluginModule() {
  override fun createPluginView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
    return layoutInflater.inflate(R.layout.io_mehow_laboratory_plugin_item, parent, false).apply {
      findViewById<View>(R.id.io_mehow_open_laboratory_button).setOnClickListener {
        context.startActivity(Intent(context, LaboratoryActivity::class.java))
      }
    }
  }
}
