plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  resourcePrefix("io_mehow_laboratory_")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

dependencies {
  api(project(":laboratory"))
  implementation(Libs.Hyperion.Plugin)
  implementation(Libs.AndroidX.CoreKtx)
  implementation(Libs.AndroidX.AppCompat)
  implementation(Libs.AndroidX.FragmentKtx)
  implementation(Libs.AndroidX.ViewModelKtx)
  implementation(Libs.AndroidX.RecyclerView)
  implementation(Libs.AndroidX.ViewPager2)
  implementation(Libs.Material)
  implementation(Libs.Kotlin.Coroutines.Android)

  testImplementation(Libs.Kotest.RunnerJunit5)
  testImplementation(Libs.Kotest.Assertions)
  testImplementation(Libs.Turbine)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
