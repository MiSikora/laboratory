plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.laboratory.convention)
}

dependencies {
  api(libs.kotlinpoet)
  implementation(projects.laboratory.runtime)

  testImplementation(libs.junit)
  testImplementation(libs.kotest.assertions)
}
