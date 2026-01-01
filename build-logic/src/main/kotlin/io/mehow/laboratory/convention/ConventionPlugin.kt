package io.mehow.laboratory.convention

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryPlugin
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
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
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

    if (!target.isRoot) {
      target.group = target.requireProperty("GROUP")
      target.version = target.requireProperty("VERSION_NAME")
      target.configureAndroid(minSdk = 23, compileSdk = 36)
      target.configureKotlin()
    }
  }
}

private fun Project.configureKotlin() {
  plugins.withType<KotlinBasePlugin>().configureEach {
    configure<KotlinBaseExtension> { explicitApi() }
  }
  tasks.withType<KotlinCompilationTask<KotlinJvmCompilerOptions>>().configureEach {
    compilerOptions {
      freeCompilerArgs.addAll("-Xjvm-default=all")
      progressiveMode.set(true)
      allWarningsAsErrors.set(true)
      optIn.addAll("kotlin.RequiresOptIn")
    }
  }
}

private fun Project.configureAndroid(minSdk: Int, compileSdk: Int) {
  plugins.withType<LibraryPlugin>().configureEach {
    configure<LibraryAndroidComponentsExtension> {
      beforeVariants { builder -> builder.enable = builder.buildType == "release" }
    }

    configure<LibraryExtension> {
      this.compileSdk = compileSdk
      defaultConfig.minSdk = minSdk
      testOptions.targetSdk = compileSdk

      lint {
        lintConfig = rootProject.file("lint.xml")
        warningsAsErrors = true

        htmlReport = isCiRun()
        xmlReport = isCiRun()
        textReport = isCiRun()

        checkGeneratedSources = true
        checkTestSources = false
        checkReleaseBuilds = false // Execute explicitly on CI instead
      }
    }
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

private fun Project.requireProperty(name: String) =
  requireNotNull(property(name)) { "Project $this has no '$name' property." }

private fun isCiRun() = System.getProperty("CI").toBoolean()

private fun VersionCatalog.requireVersion(alias: String): VersionConstraint {
  return requireNotNull(findVersion(alias).getOrNull()) { "No version defined for '$alias'." }
}

private fun VersionCatalog.requirePlugin(alias: String): PluginDependency {
  return requireNotNull(findPlugin(alias).getOrNull()?.get()) { "No plugin defined for '$alias'." }
}
