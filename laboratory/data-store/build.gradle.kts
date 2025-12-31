plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.spotless)
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
  alias(libs.plugins.wire)
}

wire { kotlin {} }

android {
  namespace = "io.mehow.laboratory.datastore"

  sourceSets { getByName("main").java.srcDirs("${layout.buildDirectory}/generated/source/wire/") }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
  api(projects.laboratory.runtime)
  api(libs.androidx.datastore)

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.turbine)
}
