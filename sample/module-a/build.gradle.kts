plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.a"

  feature("Authentication") {
    withDefaultValue("Password")
    withValue("Fingerprint")
    withValue("Retina")
    withValue("Face")

    withSource("Firebase")
    withSource("Aws")
  }

  feature("AllowScreenshots") {
    withValue("Enabled")
    withDefaultValue("Disabled")
  }
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
}
