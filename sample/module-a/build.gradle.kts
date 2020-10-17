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
    description = "Enables or disables screenshots during a video chat"

    withValue("Enabled")
    withDefaultValue("Disabled")
  }
}

dependencies {
  api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.10")
}
