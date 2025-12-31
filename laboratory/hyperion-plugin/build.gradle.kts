plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.spotless)
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
  alias(libs.plugins.ksp)
}

android {
  namespace = "io.mehow.laboratory.hyperion"
  resourcePrefix = "io_mehow_laboratory_"
}

dependencies {
  api(projects.laboratory.inspector)
  api(libs.hyperion.plugin)
  implementation(libs.androidx.appcompat)
  ksp(libs.auto.service.ksp)
}
