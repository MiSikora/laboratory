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
  api(Libs.Kotlin.StdLibJdk7)
}
