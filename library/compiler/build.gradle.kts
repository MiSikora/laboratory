plugins {
  kotlin("jvm")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

dependencies {
  api(Libs.Arrow)
  api(Libs.Kotlin.StdLibJdk8)
  implementation(project(":library:laboratory"))
  implementation(Libs.KotlinPoet)

  testImplementation(Libs.Kotest.RunnerJunit5)
  testImplementation(Libs.Kotest.Property)
  testImplementation(Libs.Kotest.Assertions)
  testImplementation(Libs.Kotest.AssertionsArrow)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
