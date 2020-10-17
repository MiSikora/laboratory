plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.b"

  feature("PowerSource") {
    withValue("Coal")
    withValue("Wind")
    withDefaultValue("Solar")
    withValue("Nuclear")
    withValue("ColdFusion")

    withDefaultSource("Firebase")
  }
}

dependencies {
  api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.10")
}
