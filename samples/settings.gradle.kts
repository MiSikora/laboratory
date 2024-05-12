pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
      library("laboratory-runtime", "io.mehow.laboratory:laboratory:+")
      library("laboratory-sharedPreferences", "io.mehow.laboratory:laboratory-shared-preferences:+")
      library("laboratory-dataStore", "io.mehow.laboratory:laboratory-data-store:+")
      library("laboratory-generator", "io.mehow.laboratory:laboratory-generator:+")
      library("laboratory-inspector", "io.mehow.laboratory:laboratory-inspector:+")
      library("laboratory-hyperionPlugin", "io.mehow.laboratory:laboratory-hyperion-plugin:+")
      plugin("laboratory", "io.mehow.laboratory").version("+")
    }
  }

  repositories {
    mavenCentral()
    google()
  }
}

includeBuild("..") {
  dependencySubstitution {
    substitute(module("io.mehow.laboratory:laboratory")).using(project(":laboratory:runtime"))
    substitute(module("io.mehow.laboratory:laboratory-shared-preferences")).using(project(":laboratory:shared-preferences"))
    substitute(module("io.mehow.laboratory:laboratory-data-store")).using(project(":laboratory:data-store"))
    substitute(module("io.mehow.laboratory:laboratory-generator")).using(project(":laboratory:generator"))
    substitute(module("io.mehow.laboratory:laboratory-gradle-plugin")).using(project(":laboratory:gradle-plugin"))
    substitute(module("io.mehow.laboratory:laboratory-inspector")).using(project(":laboratory:inspector"))
    substitute(module("io.mehow.laboratory:laboratory-hyperion-plugin")).using(project(":laboratory:hyperion-plugin"))
  }
}

rootProject.name = "samples-root"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":basic")
include(":default-option")
include(":supervision")
include(":multi-module")
include(":multi-module:multi-module-a")
include(":multi-module:multi-module-b")
include(":multi-module:multi-module-c")
