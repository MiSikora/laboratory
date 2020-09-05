plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.brombulator"

  feature("Brombulation") {
    withFallbackValue("Enabled")
    withValue("Disabled")
  }
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
}
