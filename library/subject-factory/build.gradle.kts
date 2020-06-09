plugins {
  kotlin("jvm")
}

dependencies {
  implementation(Libs.Kotlin.StdLibJdk7)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
