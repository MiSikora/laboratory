plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
}

gradlePlugin {
  plugins {
    create("laboratoryConvention") {
      id = "io.mehow.laboratory.convention"
      implementationClass = "io.mehow.laboratory.convention.ConventionPlugin"
    }
  }
}

dependencies {
  implementation(libs.gradle.agp.api)
  implementation(libs.gradle.kgp.api)
  implementation(libs.gradle.maven.publish)
  implementation(libs.gradle.spotless)
  implementation(libs.gradle.tapmoc)
}
