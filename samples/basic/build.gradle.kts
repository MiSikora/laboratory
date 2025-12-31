plugins {
  alias(libs.plugins.agp.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.laboratory)
  alias(libs.plugins.spotless)
}

android { namespace = "io.mehow.laboratory.sample.basic" }

laboratory {
  packageName = "io.mehow.laboratory.sample.basic"

  featureFactory()

  feature("LogType") {
    deprecated("Sample deprecation")

    withDefaultOption("Info")
    withOption("Verbose")
    withOption("Debug")
    withOption("Warning")
    withOption("Error")
  }

  disabledFeature("ReportRootedDevice") {
    description =
      "Reports during [cold start](https://developer.android.com/topic/performance/vitals/launch-time#cold) whether device is rooted"
  }

  feature("Authentication") {
    withDefaultOption("Password")
    withOption("Fingerprint")
    withOption("Retina")
    withOption("Face")
  }
}

dependencies {
  implementation(libs.coroutines.android)
  implementation(libs.android.material)
  implementation(libs.hyperion.core)
  implementation(libs.laboratory.dataStore)
  implementation(libs.laboratory.hyperionPlugin)
}
