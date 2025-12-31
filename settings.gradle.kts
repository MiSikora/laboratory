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

rootProject.name = "laboratory-root"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":laboratory:runtime")

include(":laboratory:shared-preferences")

include(":laboratory:data-store")

include(":laboratory:generator")

include(":laboratory:gradle-plugin")

include(":laboratory:inspector")

include(":laboratory:hyperion-plugin")
