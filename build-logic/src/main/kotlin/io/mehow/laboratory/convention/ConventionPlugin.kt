package io.mehow.laboratory.convention

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.spotless.LineEnding
import kotlin.jvm.optionals.getOrNull
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency
import tapmoc.configureJavaCompatibility
import tapmoc.configureKotlinCompatibility

@Suppress("Unused") // Used by Gradle to configure projects.
class ConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    if (target.isRoot) {
      target.subprojects { plugins.apply("io.mehow.laboratory.convention") }
    }

    val libs = target.rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
    target.configureJavaCompatibility(11)
    target.configureKotlinCompatibility(libs.requireVersion("kotlin").requiredVersion)
    target.configureSpotless(
      spotlessId = libs.requirePlugin("spotless").pluginId,
      ktfmtVersion = libs.requireVersion("ktfmt").requiredVersion,
    )
  }
}

private fun Project.configureSpotless(spotlessId: String, ktfmtVersion: String) {
  plugins.apply(spotlessId)
  val applyConfiguration: SpotlessExtension.() -> Unit = {
    lineEndings = LineEnding.UNIX

    kotlin {
      target("src/**/*.kt")
      trimTrailingWhitespace()
      endWithNewline()
      ktfmt(ktfmtVersion).googleStyle()
    }

    kotlinGradle {
      target("*.kts")
      trimTrailingWhitespace()
      endWithNewline()
      ktfmt(ktfmtVersion).googleStyle()
    }

    format("misc") {
      target(
        "*.md",
        "*.yml",
        "*.proto",
        "*.properties",
        "*.toml",
        "*.xml",
        "*.txt",
        "*.html",
        "*.css",
        ".gitignore",
        ".editorconfig",
      )
      trimTrailingWhitespace()
      endWithNewline()
    }
  }
  configure<SpotlessExtension> {
    applyConfiguration()
    if (project.rootProject == project) {
      predeclareDeps()
    }
  }
  if (project.isRoot) {
    configure<SpotlessExtensionPredeclare> { applyConfiguration() }
  }
}

private val Project.isRoot
  get() = this == rootProject

private fun VersionCatalog.requireVersion(alias: String): VersionConstraint {
  return requireNotNull(findVersion(alias).getOrNull()) { "No version defined for '$alias'." }
}

private fun VersionCatalog.requirePlugin(alias: String): PluginDependency {
  return requireNotNull(findPlugin(alias).getOrNull()?.get()) { "No plugin defined for '$alias'." }
}
