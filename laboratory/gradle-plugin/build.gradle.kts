plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.buildconfig)
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

tasks.withType<Test>().configureEach { useJUnitPlatform() }

val fixtureClasspath: Configuration by configurations.creating

tasks.withType<PluginUnderTestMetadata>().configureEach { pluginClasspath.from(fixtureClasspath) }

dependencies {
  compileOnly(libs.gradle.agp.api)

  implementation(projects.laboratory.generator)
  implementation(libs.kgp)

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions)

  fixtureClasspath(libs.gradle.agp)
}
