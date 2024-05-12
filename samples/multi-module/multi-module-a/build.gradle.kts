plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.laboratory)
  alias(libs.plugins.detekt)
  alias(libs.plugins.spotless)
}

laboratory {
  packageName = "io.mehow.laboratory.smaple.multimodule.a"

  feature("Authentication") {
    withDefaultOption("Password")
    withOption("Fingerpint")
    withOption("Retina")
    withOption("Face")
  }
}
