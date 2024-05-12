plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.laboratory)
  alias(libs.plugins.detekt)
  alias(libs.plugins.spotless)
}

android {
  namespace = "io.mehow.laboratory.sample.multimodule"
}

laboratory {
  packageName = "io.mehow.laboratory.sample.multimodule"

  featureFactory()

  dependency(project(":multi-module:multi-module-a"))
  dependency(project(":multi-module:multi-module-b"))
}

dependencies {
  implementation(libs.kotlinx.coroutinesAndroid)
  implementation(libs.android.material)
  implementation(libs.hyperion.core)
  implementation(libs.laboratory.dataStore)
  implementation(libs.laboratory.hyperionPlugin)
  implementation(projects.multiModule.multiModuleA)
  implementation(projects.multiModule.multiModuleB)
  implementation(projects.multiModule.multiModuleC)
}
