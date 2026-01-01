import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.vanniktech.maven.publish.MavenPublishBaseExtension
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
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.wire) apply false
  alias(libs.plugins.ksp) apply false
  id("io.mehow.laboratory.convention")
}

tasks.dokkaHtmlMultiModule {
  moduleName.set("Laboratory")
  moduleVersion.set(project.property("VERSION_NAME") as String)
  outputDirectory.set(rootDir.resolve("docs/api"))
}

val mavenPublishId = libs.plugins.maven.publish.get().pluginId

subprojects {
  group = project.property("GROUP") as String
  version = project.property("VERSION_NAME") as String

  plugins.withType<KotlinBasePlugin>().configureEach {
    tasks.withType<KotlinCompilationTask<KotlinJvmCompilerOptions>>().configureEach {
      compilerOptions {
        progressiveMode.set(true)
        allWarningsAsErrors.set(true)
        optIn.addAll("kotlin.RequiresOptIn")
        freeCompilerArgs.addAll("-Xjvm-default=all")
      }
    }

    configure<KotlinProjectExtension> { explicitApi() }
  }

  tasks.withType<Test>().configureEach { testLogging.events("skipped", "failed", "passed") }

  plugins.withType<LibraryPlugin>().configureEach {
    configure<LibraryExtension> {
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
