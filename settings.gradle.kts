rootProject.name = "laboratory-root"

pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
  }
}

includeBuild("build-logic")

include(
  ":laboratory:runtime",
  ":laboratory:shared-preferences",
  ":laboratory:data-store",
  ":laboratory:generator",
  ":laboratory:gradle-plugin",
  ":laboratory:inspector",
  ":laboratory:hyperion-plugin",
  ":laboratory:testing",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
