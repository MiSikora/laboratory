rootProject.name = "build-logic"

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
