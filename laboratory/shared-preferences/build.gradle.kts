plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.spotless)
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
}

android {
  namespace = "io.mehow.laboratory.sharedpreferences"

  defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testInstrumentationRunnerArguments += "clearPackageData" to "true"
  }

  testOptions.execution = "ANDROIDX_TEST_ORCHESTRATOR"
  testBuildType = "release"

  buildTypes {
    release {
      // Testing release builds requires signing them
      signingConfig = signingConfigs.getByName("debug")
    }
  }
}

dependencies {
  api(projects.laboratory.runtime)
  implementation(libs.kotlinx.coroutines.core)

  androidTestImplementation(libs.kotest.assertions)
  androidTestImplementation(libs.turbine)
  androidTestUtil(libs.androidx.test.orchestrator)
  androidTestImplementation(libs.androidx.test.coreKtx)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.testExt.junitKtx)
}
