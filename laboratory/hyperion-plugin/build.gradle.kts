plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.ksp)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.laboratory.convention)
}

buildConfig {
  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }
  packageName("io.mehow.laboratory.hyperion")
  buildConfigField("Int", "MinSdk", rootProject.libs.versions.android.min.sdk.get())
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
