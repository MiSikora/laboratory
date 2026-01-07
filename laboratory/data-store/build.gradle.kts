plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.wire)
  alias(libs.plugins.laboratory.convention)
}

wire { kotlin {} }

android {
  namespace = "io.mehow.laboratory.datastore"

  sourceSets { getByName("main").java.srcDirs("${layout.buildDirectory}/generated/source/wire/") }
}

dependencies {
  api(projects.laboratory.runtime)
  api(libs.androidx.datastore)

  testImplementation(projects.laboratory.testing)
  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.turbine)
}
