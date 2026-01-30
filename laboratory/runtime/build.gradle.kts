plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.laboratory.convention)
}

dependencies {
  api(libs.coroutines.core)

  testImplementation(projects.laboratory.testing)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.turbine)
}
