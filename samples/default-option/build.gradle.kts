plugins {
  alias(libs.plugins.agp.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.laboratory)
}

android { namespace = "io.mehow.laboratory.sample.defaultoption" }

laboratory {
  packageName = "io.mehow.laboratory.sample.defaultoption"

  featureFactory()

  enabledFeature("ShowAds")
  enabledFeature("ReportRootedDevice")
  enabledFeature("RequiredFingerprint")
}

dependencies {
  implementation(libs.coroutines.android)
  implementation(libs.android.material)
  implementation(libs.hyperion.core)
  implementation(libs.laboratory.dataStore)
  implementation(libs.laboratory.hyperionPlugin)
}
