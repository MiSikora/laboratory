plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.trombulator"

  feature("Trombulation") {
    withValue("LevelOne")
    withFallbackValue("LevelTwo")
    withValue("LevelThree")

    withSource("Firebase")
    withFallbackSource("Aws")
  }

  feature("UnsourcedTrombulation") {
    withValue("HalfPower")
    withFallbackValue("UnlimitedPower")
  }
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
}
