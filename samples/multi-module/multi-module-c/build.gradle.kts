plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.laboratory)
  alias(libs.plugins.detekt)
  alias(libs.plugins.spotless)
}

laboratory {
  packageName = "io.mehow.laboratory.smaple.multimodule.c"

  featureFactory { isPublic = true }

  feature("Camera") {
    withDefaultOption("Disabled")

    withOption("Enabled") {
      feature("LivestreamPreview") {
        withDefaultOption("Enabled")
        withOption("Disabled")
      }

      feature("RecordingQuality") {
        withDefaultOption("SD")
        withOption("HD")
        withOption("QHD")
      }

      feature("RecordingDirectory") {
        withDefaultOption("Internal")
        withOption("External")
      }

      feature("VideoFilter") {
        withDefaultOption("NoFilter")
        withOption("Retro")
        withOption("Sepia")
        withOption("EightBit")
      }

      feature("MotionDetection") {
        withDefaultOption("Enabled")
        withOption("Disabled")
      }

      feature("NightMode") {
        withDefaultOption("Enabled")
        withOption("Disabled")
      }
    }
  }
}
