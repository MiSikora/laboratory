plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.brombulator"

  feature("Brombulation") {
    withValue("Enabled")
    withValue("Disabled")
  }
}

dependencies {
  implementation(Libs.Kotlin.StdLibJdk7)
}
