import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.AppPlugin

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.agp.application) apply false
  alias(libs.plugins.laboratory) apply false
  alias(libs.plugins.laboratory.convention) apply false
}

subprojects {
  plugins.withType<AppPlugin>().configureEach {
    configure<ApplicationExtension> { buildFeatures { viewBinding = true } }
  }
}
