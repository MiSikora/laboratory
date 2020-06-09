import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.plugins.BasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    gradlePluginPortal()
    google()
    mavenLocal()
  }

  dependencies {
    classpath(Libs.AndroidGradlePlugin)
    classpath(Libs.Kotlin.GradlePlugin)
    classpath(Libs.MavenPublishGradlePlugin)
  }
}

allprojects {
  repositories {
    mavenCentral()
    google()
    jcenter()
    mavenLocal()
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
        "-XXLanguage:+NewInference"
      )
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
    }
  }
}
