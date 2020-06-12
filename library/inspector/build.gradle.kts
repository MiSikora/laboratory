plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  resourcePrefix("io_mehow_laboratory_")
}

dependencies {
  api(project(":library:laboratory"))
  implementation(Libs.HyperionPlugin)
  implementation(Libs.Kotlin.StdLibJdk7)
  implementation(Libs.AndroidX.CoreKtx)
  implementation(Libs.Material)

  testImplementation(Libs.Kotest.RunnerJunit5)
  testImplementation(Libs.Kotest.Assertions)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
