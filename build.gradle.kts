buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }

  dependencies {
    classpath(libs.android.gradlePlugin)
    classpath(libs.kotlin.gradlePlugin)
    classpath(libs.googleServices.gradlePlugin)
  }
}
