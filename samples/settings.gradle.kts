rootProject.name = "samples-root"

pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

sealed class Dependency {
  abstract val alias: String
  abstract val artifactId: String
  abstract val projectId: String

  val packageName = "io.mehow.laboratory"

  abstract fun appendTo(builder: VersionCatalogBuilder)

  fun substituteIn(substitutions: DependencySubstitutions) {
    with(substitutions) { substitute(module("$packageName:$artifactId")).using(project(projectId)) }
  }
}

class Lib(
  override val alias: String,
  override val artifactId: String,
  override val projectId: String,
) : Dependency() {
  override fun appendTo(builder: VersionCatalogBuilder) {
    builder.library(alias, "$packageName:$artifactId:+")
  }
}

class Plugin(
  override val alias: String,
  override val artifactId: String,
  override val projectId: String,
  val pluginId: String,
) : Dependency() {
  override fun appendTo(builder: VersionCatalogBuilder) {
    builder.plugin(pluginId, packageName).version("+")
  }
}

val dependencies =
  listOf(
    Lib(alias = "laboratory-runtime", artifactId = "laboratory", projectId = ":laboratory:runtime"),
    Lib(
      alias = "laboratory-sharedPrefernces",
      artifactId = "laboratory-shared-preferences",
      projectId = ":laboratory:shared-preferences",
    ),
    Lib(
      alias = "laboratory-dataStore",
      artifactId = "laboratory-data-store",
      projectId = ":laboratory:data-store",
    ),
    Lib(
      alias = "laboratory-generator",
      artifactId = "laboratory-generator",
      projectId = ":laboratory:generator",
    ),
    Lib(
      alias = "laboratory-inspector",
      artifactId = "laboratory-inspector",
      projectId = ":laboratory:inspector",
    ),
    Lib(
      alias = "laboratory-hyperionPlugin",
      artifactId = "laboratory-hyperion-plugin",
      projectId = ":laboratory:hyperion-plugin",
    ),
    Plugin(
      alias = "laboratory-gradlePlugin",
      artifactId = "laboratory-gradle-plugin",
      projectId = ":laboratory:gradle-plugin",
      pluginId = "laboratory",
    ),
  )

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
      dependencies.forEach { dependency -> dependency.appendTo(this) }
    }
  }

  repositories {
    mavenCentral()
    google()
  }
}

includeBuild("..") {
  dependencySubstitution { dependencies.forEach { dependency -> dependency.substituteIn(this) } }
}

include(
  ":basic",
  ":default-option",
  ":supervision",
  ":multi-module",
  ":multi-module:multi-module-a",
  ":multi-module:multi-module-b",
  ":multi-module:multi-module-c",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
