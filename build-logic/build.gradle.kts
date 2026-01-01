plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.spotless)
}

gradlePlugin {
  plugins {
    create("laboratoryConvention") {
      id = "io.mehow.laboratory.convention"
      implementationClass = "io.mehow.laboratory.convention.ConventionPlugin"
    }
  }
}

val ktfmtVersion = libs.versions.ktfmt.get()

spotless {
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
}

dependencies {
  implementation(libs.gradle.agp.api)
  implementation(libs.gradle.kgp.api)
  implementation(libs.gradle.spotless)
  implementation(libs.gradle.tapmoc)
}
