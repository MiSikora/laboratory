import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins { alias(libs.plugins.kotlin.jvm) }

kotlin { explicitApi = ExplicitApiMode.Disabled }

dependencies {
  api(projects.laboratory.runtime)

  api(libs.kotest.framework.api)
  implementation(libs.kotest.assertions)
  implementation(libs.turbine)
}
