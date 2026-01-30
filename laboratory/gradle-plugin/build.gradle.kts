plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.laboratory.convention)
}

val pluginName = "laboratory"

gradlePlugin {
  plugins {
    create(pluginName) {
      id = "io.mehow.laboratory"
      implementationClass = "io.mehow.laboratory.gradle.LaboratoryPlugin"
    }
  }
}

buildConfig {
  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }
  packageName("io.mehow.laboratory.gradle")
  buildConfigField("String", "LibraryVersion", "\"${project.version}\"")
  buildConfigField("String", "PluginName", "\"${pluginName}\"")
}

val fixtureClasspath: Configuration by configurations.creating

tasks.withType<PluginUnderTestMetadata>().configureEach { pluginClasspath.from(fixtureClasspath) }

dependencies {
  implementation(projects.laboratory.generator)
  implementation(libs.gradle.agp.api)
  implementation(libs.gradle.kgp.api)

  testImplementation(projects.laboratory.testing)
  testImplementation(libs.junit)
  testImplementation(libs.kotest.assertions)

  fixtureClasspath(libs.gradle.agp)
  fixtureClasspath(libs.gradle.kgp)
}
