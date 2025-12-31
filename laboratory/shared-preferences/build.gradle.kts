plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.spotless)
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
  implementation(libs.coroutines.core)

  androidTestImplementation(libs.kotest.assertions)
  androidTestImplementation(libs.turbine)
  androidTestUtil(libs.androidx.test.orchestrator)
  androidTestImplementation(libs.androidx.test.core.ktx)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.junit.ktx)
}
