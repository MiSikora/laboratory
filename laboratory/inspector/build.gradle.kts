plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.spotless)
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
}

android {
  namespace = "io.mehow.laboratory.inspector"
  resourcePrefix = "io_mehow_laboratory_"

  defaultConfig {
    consumerProguardFile("io-mehow-laboratory-inspector.pro")
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

dependencies {
  api(projects.laboratory.runtime)
  implementation(libs.hyperion.plugin)
  implementation(libs.androidx.appCompat)
  implementation(libs.androidx.fragmentKtx)
  implementation(libs.androidx.viewModelKtx)
  implementation(libs.androidx.recyclerView)
  implementation(libs.androidx.viewPager2)
  implementation(libs.android.material)
  implementation(libs.kotlinx.coroutinesAndroid)

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.property)
  testImplementation(libs.turbine)
  testImplementation(libs.kotlinx.coroutinesTest)
}
