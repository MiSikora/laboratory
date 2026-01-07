import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.laboratory.convention)
}

kotlin { explicitApi = ExplicitApiMode.Disabled }

dependencies {
  api(projects.laboratory.runtime)

  api(libs.kotest.framework.engine)
  implementation(libs.kotest.assertions)
  implementation(libs.turbine)
}
