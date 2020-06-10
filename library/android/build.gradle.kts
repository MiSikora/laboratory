plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  defaultConfig {
    consumerProguardFile("laboratory-shrinking.pro")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testInstrumentationRunnerArgument("clearPackageData", "true")
  }

  testOptions { execution = "ANDROIDX_TEST_ORCHESTRATOR" }
}

dependencies {
  api(project(":library:laboratory"))
  implementation(Libs.Kotlin.StdLibJdk7)
  implementation(Libs.AndroidX.CoreKtx)

  androidTestUtil(Libs.AndroidX.Test.Orchestrator)
  androidTestImplementation(Libs.AndroidX.TestExtJUnitKtx)
  androidTestImplementation(Libs.AndroidX.Test.CoreKtx)
  androidTestImplementation(Libs.AndroidX.Test.Runner)
  androidTestImplementation(Libs.Kotest.Assertions)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
