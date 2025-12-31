import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.diffplug.spotless.LineEnding
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.agp.library) apply false
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.binary.compatibility.validator)
  alias(libs.plugins.dokka)
  alias(libs.plugins.spotless)
  alias(libs.plugins.detekt)
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.wire) apply false
  alias(libs.plugins.ksp) apply false
}

tasks.dokkaHtmlMultiModule {
  moduleName.set("Laboratory")
  moduleVersion.set(project.property("VERSION_NAME") as String)
  outputDirectory.set(rootDir.resolve("docs/api"))
}

val javaTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
val ktfmtVersion = libs.versions.ktfmt.get()
val mavenPublishId = libs.plugins.maven.publish.get().pluginId

allprojects {
  val configureSpotless: SpotlessExtension.() -> Unit = {
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
  plugins.withType<SpotlessPlugin>().configureEach {
    configure<SpotlessExtension> {
      configureSpotless()

      if (project.rootProject == project) {
        predeclareDeps()
      }
    }
    if (project.rootProject == project) {
      configure<SpotlessExtensionPredeclare> { configureSpotless() }
    }
  }

  plugins.withType<DetektPlugin>().configureEach {
    configure<DetektExtension> {
      toolVersion = libs.versions.detekt.get()
      allRules = true
      parallel = true
      buildUponDefaultConfig = true
      config.from(rootProject.file("detekt.yml"))
    }
    tasks.withType<Detekt>().configureEach {
      jvmTarget = javaTarget.target
      reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
      }
    }
  }
}

subprojects {
  group = project.property("GROUP") as String
  version = project.property("VERSION_NAME") as String

  plugins.withType<KotlinBasePlugin>().configureEach {
    tasks.withType<KotlinCompilationTask<KotlinJvmCompilerOptions>>().configureEach {
      compilerOptions {
        jvmTarget.set(javaTarget)
        progressiveMode.set(true)
        allWarningsAsErrors.set(true)
        optIn.addAll("kotlin.RequiresOptIn")
        freeCompilerArgs.addAll("-Xjvm-default=all")
      }
    }

    configure<KotlinProjectExtension> { explicitApi() }
  }

  tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = javaTarget.target
    targetCompatibility = javaTarget.target
  }

  tasks.withType<Test>().configureEach { testLogging.events("skipped", "failed", "passed") }

  plugins.withType<LibraryPlugin>().configureEach {
    configure<LibraryExtension> {
      compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaTarget.target)
        targetCompatibility = JavaVersion.toVersion(javaTarget.target)
      }

      compileSdk = libs.versions.compileSdk.get().toInt()
      defaultConfig.minSdk = libs.versions.minSdk.get().toInt()
      testOptions.targetSdk = libs.versions.targetSdk.get().toInt()

      lint {
        lintConfig = rootProject.file("lint.xml")
        warningsAsErrors = true

        htmlReport = true
        xmlReport = true
        textReport = true

        checkGeneratedSources = true
        checkTestSources = false
        checkReleaseBuilds = false // Execute explicitly on CI instead
      }
    }

    configure<LibraryAndroidComponentsExtension> {
      beforeVariants { builder -> builder.enable = builder.buildType == "release" }
    }
  }

  pluginManager.withPlugin(mavenPublishId) {
    configure<MavenPublishBaseExtension> {
      publishToMavenCentral()
      signAllPublications()
    }
  }
}
