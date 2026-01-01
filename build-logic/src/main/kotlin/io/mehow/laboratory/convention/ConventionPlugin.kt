package io.mehow.laboratory.convention

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryPlugin
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.spotless.LineEnding
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import kotlin.jvm.optionals.getOrNull
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
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
  private lateinit var libs: VersionCatalog

  override fun apply(target: Project) {
    libs = target.requireVersionCatalog()

    target.group = target.requireProperty("GROUP")
    target.version = target.requireProperty("VERSION_NAME")
    target.configureCompatibility()
    target.configureKotlin()
    target.configureAndroid()
    target.configureTesting()
    target.configureSpotless()
    target.configureMavenPublishing()

    if (target.isRoot) {
      target.subprojects { plugins.apply("io.mehow.laboratory.convention") }
    }
  }

  private fun Project.configureCompatibility() {
    configureJavaCompatibility(11)
    configureKotlinCompatibility(libs.kotlinVersion)
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

  private fun Project.configureAndroid() {
    plugins.withType<LibraryPlugin>().configureEach {
      configure<LibraryAndroidComponentsExtension> {
        beforeVariants { builder -> builder.enable = builder.buildType == "release" }
      }

      configure<LibraryExtension> {
        compileSdk = 36
        defaultConfig.minSdk = 21
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

  private fun Project.configureTesting() {
    tasks.withType<Test>().configureEach {
      testLogging {
        if (isCiRun()) {
          events(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
        }
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = false
      }
      useJUnitPlatform()
    }
  }

  private fun Project.configureSpotless() {
    plugins.apply(libs.spotlessId)
    val applyConfiguration: SpotlessExtension.() -> Unit = {
      lineEndings = LineEnding.UNIX

      kotlin {
        target("src/**/*.kt")
        trimTrailingWhitespace()
        endWithNewline()
        ktfmt(libs.ktfmtVersion).googleStyle()
      }

      kotlinGradle {
        target("*.kts")
        trimTrailingWhitespace()
        endWithNewline()
        ktfmt(libs.ktfmtVersion).googleStyle()
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

  private fun Project.configureMavenPublishing() {
    plugins.withId(libs.mavenPublishId) {
      configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
        signAllPublications()
      }
    }
  }
}

private val Project.isRoot
  get() = this == rootProject

private fun Project.requireProperty(name: String) =
  requireNotNull(property(name)) { "Project $this has no '$name' property." }

private fun isCiRun() = System.getProperty("CI").toBoolean()

private val VersionCatalog.kotlinVersion
  get() = requireVersion("kotlin").requiredVersion

private val VersionCatalog.ktfmtVersion
  get() = requireVersion("ktfmt").requiredVersion

private val VersionCatalog.spotlessId
  get() = requirePlugin("spotless").pluginId

private val VersionCatalog.mavenPublishId
  get() = requirePlugin("maven-publish").pluginId

private fun Project.requireVersionCatalog(): VersionCatalog {
  return rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
}

private fun VersionCatalog.requireVersion(alias: String): VersionConstraint {
  return requireNotNull(findVersion(alias).getOrNull()) { "No version defined for '$alias'." }
}

private fun VersionCatalog.requirePlugin(alias: String): PluginDependency {
  return requireNotNull(findPlugin(alias).getOrNull()?.get()) { "No plugin defined for '$alias'." }
}
