plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.laboratory)
  alias(libs.plugins.spotless)
}

laboratory {
  packageName = "io.mehow.laboratory.smaple.multimodule.b"

  feature("LogType") {
    withDefaultOption("Verbose")
    withOption("Debug")
    withOption("Info")
    withOption("Warning")
    withOption("Error")
  }

  enabledFeature("ShowAds")
  enabledFeature("ReportRootedDevice")
}
