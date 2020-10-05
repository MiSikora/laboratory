import com.android.build.api.variant.VariantFilter
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.plugins.BasePlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id(Libs.Detekt.GradlePluginId) version Libs.Detekt.Version
  id(Libs.GradleVersions.GradlePluginId) version Libs.GradleVersions.Version
}

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
    mavenLocal() // Used for sample snapshot testing.
  }

  dependencies {
    classpath(Libs.AndroidGradlePlugin)
    classpath(Libs.Kotlin.GradlePlugin)
    classpath(Libs.MavenPublishGradlePlugin)
    classpath(Libs.Kotlin.DokkaGradlePlugin)
    classpath(Libs.Detekt.GradlePlugin)
    classpath(Libs.GradleVersions.GradlePlugin)
    classpath(Libs.Wire.GradlePlugin)
    @Suppress("GradleDynamicVersion") // We want the latest version as we control it.
    classpath("io.mehow.laboratory:laboratory-gradle-plugin:+")
  }
}

allprojects {
  repositories {
    mavenCentral()
    google()
    jcenter()
    mavenLocal() // Used for sample snapshot testing.
  }

  group = properties["GROUP"]!!
  version = properties["VERSION_NAME"]!!

  tasks.withType<Test> {
    testLogging {
      events("skipped", "failed", "passed")
    }
  }

  tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs = listOf(
        "-progressive",
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xjvm-default=enable"
      )
    }
  }

  plugins.withType<LibraryPlugin> {
    extensions.findByType<BaseExtension>()?.apply {
      variantFilter = Action<VariantFilter> {
        ignore = name != "release"
      }
    }
  }

  plugins.withType<BasePlugin> {
    extension.compileOptions {
      sourceCompatibility = JavaVersion.VERSION_1_8
      targetCompatibility = JavaVersion.VERSION_1_8
    }

    extensions.findByType<BaseExtension>()?.apply {
      compileSdkVersion(Build.CompileSdk)
      buildToolsVersion(Build.BuildToolsVersion)

      defaultConfig {
        minSdkVersion(Build.MinSdk)
        targetSdkVersion(Build.CompileSdk)

        vectorDrawables.useSupportLibrary = true
      }

      lintOptions {
        lintConfig = rootProject.file("lint.xml")

        htmlReport = !isCi()
        xmlReport = isCi()
        xmlOutput = file("build/reports/lint/lint-results.xml")

        textReport = true
        textOutput("stdout")
        isExplainIssues = false

        isCheckDependencies = false
        isCheckGeneratedSources = true
        isCheckTestSources = false
        isCheckReleaseBuilds = false
      }
    }
  }
}

dependencies {
  detekt(Libs.Detekt.Formatting)
  detekt(Libs.Detekt.Cli)
}

tasks.withType<Detekt> {
  parallel = true
  config.setFrom(rootProject.file("detekt-config.yml"))
  setSource(files(projectDir))
  exclude("**/test/**", "**/androidTest/**")
  exclude("buildSrc/")
  exclude("**/*.kts")
  exclude(subprojects.map { "${it.buildDir.relativeTo(rootDir).path}/" })
  reports {
    xml {
      enabled = isCi()
      destination = file("build/reports/detekt/detekt-results.xml")
    }
    html.enabled = !isCi()
    txt.enabled = false
  }
}

tasks.register("check") {
  group = "Verification"
  description = "Allows to attach Detekt to the root project."
}

tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf {
    isNonStable(candidate.version) && !isNonStable(currentVersion)
  }
}

fun isNonStable(version: String): Boolean {
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  return !regex.matches(version)
}
