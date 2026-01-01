plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.laboratory)
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
