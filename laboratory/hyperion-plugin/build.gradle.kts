plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.mavenPublish)
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
  implementation(libs.androidx.appCompat)
  ksp(libs.autoServiceKsp)
}
