plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.laboratory.convention)
}

android { namespace = "io.mehow.laboratory.datastore" }

dependencies {
  api(projects.laboratory.runtime)
  api(libs.androidx.datastore)
  api(libs.wire)

  testImplementation(projects.laboratory.testing)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.turbine)
}
