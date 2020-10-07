plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.frombulator"

  feature("Frombulation") {
    withFallbackValue("Enabled")
    withValue("Disabled")

    withFallbackSource("Firebase")
  }
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
}
