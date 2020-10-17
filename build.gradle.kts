import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.plugins.BasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }

  dependencies {
    classpath("com.android.tools.build:gradle:4.1.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
  }
}

allprojects {
  repositories {
    mavenCentral()
    google()
    jcenter()
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
          "-Xjvm-default=enable"
      )
    }
  }

  plugins.withType<BasePlugin<*, *>> {
    extension.compileOptions {
      sourceCompatibility = JavaVersion.VERSION_1_8
      targetCompatibility = JavaVersion.VERSION_1_8
    }

    extensions.findByType<BaseExtension>()?.apply {
      compileSdkVersion(30)

      defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)

        vectorDrawables.useSupportLibrary = true
      }
    }
  }
}
