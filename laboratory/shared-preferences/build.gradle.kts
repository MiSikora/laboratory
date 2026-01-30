plugins {
  alias(libs.plugins.agp.library)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.laboratory.convention)
}

android { namespace = "io.mehow.laboratory.sharedpreferences" }

dependencies {
  api(projects.laboratory.runtime)
  implementation(libs.coroutines.core)

  testImplementation(projects.laboratory.testing)
  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
}
