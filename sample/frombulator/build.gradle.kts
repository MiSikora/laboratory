plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.frombulator"

  feature("Frombulation") {
    withDefaultValue("Enabled")
    withValue("Disabled")

    withDefaultSource("Firebase")
  }
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
}
