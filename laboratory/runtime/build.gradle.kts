plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
  api(libs.coroutines.core)

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.turbine)
}
