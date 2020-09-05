plugins {
  kotlin("jvm")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

dependencies {
  api(Libs.Kotlin.StdLibJdk7)

  testImplementation(Libs.Kotest.RunnerJunit5)
  testImplementation(Libs.Kotest.Assertions)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
