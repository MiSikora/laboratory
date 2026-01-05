plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
}

dependencies {
  api(libs.coroutines.core)

  testImplementation(projects.laboratory.testing)
  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.turbine)
}
