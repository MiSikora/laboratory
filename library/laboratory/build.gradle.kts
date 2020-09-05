plugins {
  kotlin("jvm")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)
  api(Libs.Kotlin.Coroutines.Core)

  testImplementation(Libs.Kotest.RunnerJunit5)
  testImplementation(Libs.Kotest.Assertions)
  testImplementation(Libs.Turbine)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
