import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.AppPlugin
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.diffplug.spotless.LineEnding
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.agp.application) apply false
  alias(libs.plugins.laboratory) apply false
  alias(libs.plugins.detekt)
  alias(libs.plugins.spotless)
}

val javaTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
val ktfmtVersion = libs.versions.ktfmt.get()

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
      target("*.md", "*.yml", "*.proto", "*.properties", "*.toml", ".gitignore", ".editorconfig")
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
  plugins.withType<KotlinBasePlugin>().configureEach {
    tasks.withType<KotlinCompilationTask<KotlinJvmCompilerOptions>>().configureEach {
      compilerOptions {
        jvmTarget.set(javaTarget)
        progressiveMode.set(true)
        allWarningsAsErrors.set(true)
        freeCompilerArgs.addAll("-Xjvm-default=all")
      }
    }
  }

  tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = javaTarget.target
    targetCompatibility = javaTarget.target
  }

  plugins.withType<AppPlugin>().configureEach {
    configure<ApplicationExtension> {
      compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaTarget.target)
        targetCompatibility = JavaVersion.toVersion(javaTarget.target)
      }

      val dummyConfig by
        signingConfigs.creating {
          storeFile = rootProject.file("mehow-io.keystore")
          storePassword = "mehow-io"
          keyAlias = "mehow-io"
          keyPassword = "mehow-io"
        }

      compileSdk = libs.versions.compileSdk.get().toInt()
      defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        versionCode = 1
        versionName = "1.0.0"

        signingConfig = dummyConfig
      }

      buildFeatures { viewBinding = true }

      buildTypes {
        debug {
          applicationIdSuffix = ".debug"
          matchingFallbacks.add("release")
        }
      }
    }
  }
}
