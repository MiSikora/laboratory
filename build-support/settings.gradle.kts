rootProject.name = "build-support"

include(":laboratory:runtime")
project(":laboratory:runtime").projectDir = File("../laboratory/runtime")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs").from(files("../gradle/libs.versions.toml"))
  }
}
