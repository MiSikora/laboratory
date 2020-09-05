plugins {
  kotlin("jvm")
  id("io.mehow.laboratory")
}

laboratory {
  packageName = "io.mehow.laboratory.trombulator"

  feature("Trombulation") {
    withValue("LevelOne")
    withValue("LevelTwo")
    withValue("LevelThree")
  }
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
}
