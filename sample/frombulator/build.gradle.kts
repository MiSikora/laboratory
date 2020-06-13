plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.frombulator"

  feature("Frombulation") {
    withValue("Enabled")
    withValue("Disabled")
  }
}

dependencies {
  implementation(Libs.Kotlin.StdLibJdk7)
}
