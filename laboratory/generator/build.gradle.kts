plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.spotless)
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

dependencies {
  api(libs.kotlinPoet)
  implementation(projects.laboratory.runtime)

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.property)
}
