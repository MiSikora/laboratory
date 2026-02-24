package io.mehow.laboratory.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
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

    if (!target.isSample) {
      target.group = target.requireProperty("GROUP")
      target.version = target.requireProperty("VERSION_NAME")
    }
    target.configureCompatibility()
    target.configureKotlin()
    target.configureAndroid()
    target.configureTesting()
    target.configureSpotless()
    target.configureMavenPublishing()
  }

  private fun Project.configureCompatibility() {
    configureJavaCompatibility(libs.jvmTarget)
    configureKotlinCompatibility(libs.kotlinVersion)
  }

  private fun Project.configureKotlin() {
    plugins.withType<KotlinBasePlugin>().configureEach {
      configure<KotlinBaseExtension> {
        if (!project.isSample) {
          explicitApi()
        }
      }
    }
    tasks.withType<KotlinCompilationTask<KotlinJvmCompilerOptions>>().configureEach {
      compilerOptions {
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
        compileSdk = libs.androidCompileSdk
        defaultConfig.minSdk = libs.androidMinSdk
        testOptions.targetSdk = compileSdk
        testBuildType = "release"

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

    plugins.withType<AppPlugin>().configureEach {
      configure<ApplicationAndroidComponentsExtension> {
        beforeVariants { builder -> builder.enable = builder.buildType == "debug" }
      }

      configure<ApplicationExtension> {
        compileSdk = libs.androidCompileSdk
        defaultConfig {
          minSdk = libs.androidMinSdk
          targetSdk = compileSdk

          versionCode = 1
          versionName = "1.0.0"
        }

        buildTypes { debug { matchingFallbacks.add("release") } }
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
    }
  }

  private fun Project.configureSpotless() {
    plugins.apply(libs.spotlessId)
    val applyConfiguration: SpotlessExtension.() -> Unit = {
      lineEndings = LineEnding.UNIX

      kotlin {
        val targets = buildList {
          add("src/**/*.kt")
          if (project.isRoot) {
            add("build-logic/src/**/*.kt")
          }
        }
        target(*targets.toTypedArray())
        trimTrailingWhitespace()
        endWithNewline()
        ktfmt(libs.ktfmtVersion).googleStyle()
      }

      kotlinGradle {
        val targets = buildList {
          add("*.kts")
          if (project.isRoot) {
            add("build-logic/**/*.kts")
          }
        }
        target(*targets.toTypedArray())
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

      if (project.isRoot) {
        java {
          target("scripts/**/*.java")
          trimTrailingWhitespace()
          endWithNewline()
          googleJavaFormat()
        }
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

private val Project.isSample
  get() = rootProject.name == "samples-root"

private fun Project.requireProperty(name: String) =
  requireNotNull(property(name)) { "Project $this has no '$name' property." }

private fun isCiRun() = System.getProperty("CI").toBoolean()

private val VersionCatalog.jvmTarget
  get() = requireVersion("jvm-target").requiredVersion.toInt()

private val VersionCatalog.androidMinSdk
  get() = requireVersion("android-min-sdk").requiredVersion.toInt()

private val VersionCatalog.androidCompileSdk
  get() = requireVersion("android-compile-sdk").requiredVersion.toInt()

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
