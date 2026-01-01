plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
}

android {
  namespace = "io.mehow.laboratory.inspector"
  resourcePrefix = "io_mehow_laboratory_"

  defaultConfig { consumerProguardFile("io-mehow-laboratory-inspector.pro") }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
  api(projects.laboratory.runtime)
  implementation(libs.hyperion.plugin)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.viewmodel.ktx)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.viewpager2)
  implementation(libs.android.material)
  implementation(libs.coroutines.android)

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.property)
  testImplementation(libs.turbine)
  testImplementation(libs.coroutines.test)
}
