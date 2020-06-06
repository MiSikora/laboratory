package io.mehow.laboratory.hyperion

import com.google.auto.service.AutoService
import com.willowtreeapps.hyperion.plugin.v1.Plugin as HyperionPlugin
import com.willowtreeapps.hyperion.plugin.v1.PluginModule as HyperionPluginModule

@AutoService(HyperionPlugin::class)
internal class Plugin : HyperionPlugin() {
  override fun minimumRequiredApi() = 21
  override fun createPluginModule(): HyperionPluginModule = PluginModule()
}
