rootProject.name = "build-support"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }

  versionCatalogs { create("libs").from(files("../gradle/libs.versions.toml")) }
}
