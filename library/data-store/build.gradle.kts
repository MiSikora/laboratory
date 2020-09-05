plugins {
  id("com.android.library")
  kotlin("android")
  id("com.squareup.wire")
}

wire {
  kotlin { }
}

android {
  defaultConfig {
    consumerProguardFile("laboratory-shrinking.pro")
  }

  sourceSets {
    getByName("main").java.srcDirs("$buildDir/generated/source/wire/")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

dependencies {
  api(project(":library:laboratory"))
  api(Libs.AndroidX.DataStore)
  api(Libs.Wire.Runtime)

  testImplementation(Libs.Kotest.RunnerJunit5)
  testImplementation(Libs.Kotest.Assertions)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
