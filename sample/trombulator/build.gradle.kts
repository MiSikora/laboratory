plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.trombulator"

  feature("Trombulation") {
    withValue("LevelOne")
    withDefaultValue("LevelTwo")
    withValue("LevelThree")

    withSource("Firebase")
    withDefaultSource("Aws")
  }

  feature("UnsourcedTrombulation") {
    withValue("HalfPower")
    withDefaultValue("UnlimitedPower")
  }
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
}
