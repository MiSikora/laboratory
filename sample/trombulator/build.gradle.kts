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
  implementation(Libs.Kotlin.StdLibJdk7)
}
