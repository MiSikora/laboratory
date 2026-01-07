plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.laboratory.convention)
}

dependencies {
  api(libs.kotlinpoet)
  implementation(projects.laboratory.runtime)

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions)
}
