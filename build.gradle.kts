import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    gradlePluginPortal()
  }

  dependencies {
    classpath(Libs.Kotlin.GradlePlugin)
  }
}

allprojects {
  repositories {
    mavenCentral()
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
}
