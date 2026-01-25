plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.laboratory.convention)
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

  packaging.resources.excludes.add("META-INF/**")
}

dependencies {
  api(projects.laboratory.runtime)
  implementation(libs.coroutines.core)

  androidTestImplementation(projects.laboratory.testing)
  androidTestImplementation(libs.kotest.runner.android)
  androidTestImplementation(libs.kotest.assertions)
  androidTestImplementation(libs.turbine)
  androidTestImplementation(libs.androidx.test.core.ktx)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.junit.ktx)
  androidTestUtil(libs.androidx.test.orchestrator)
}

configurations.configureEach {
  // Exclude mordant FFM modules that require minSdk 26+ (JVM 21+ feature not needed on Android)
  exclude(group = "com.github.ajalt.mordant", module = "mordant-jvm-ffm-jvm")
  exclude(group = "com.github.ajalt.mordant", module = "mordant-jvm-ffm")
}
