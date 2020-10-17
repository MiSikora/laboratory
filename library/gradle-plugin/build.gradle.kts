import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  id("java-gradle-plugin")
}

gradlePlugin {
  plugins {
    create("laboratory") {
      id = "io.mehow.laboratory"
      implementationClass = "io.mehow.laboratory.gradle.LaboratoryPlugin"
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

val fixtureClasspath: Configuration by configurations.creating

tasks.withType<PluginUnderTestMetadata> {
  pluginClasspath.from(fixtureClasspath)
}

dependencies {
  implementation(Libs.Kotlin.StdLibJdk8)
  implementation(project(":generator"))
  implementation(Libs.Kotlin.GradlePlugin)
  compileOnly(Libs.AndroidGradlePlugin)

  testImplementation(Libs.Kotest.RunnerJunit5)
  testImplementation(Libs.Kotest.Assertions)

  fixtureClasspath(Libs.Kotlin.GradlePlugin)
  fixtureClasspath(Libs.AndroidGradlePlugin)
}

val versionDir = file("${buildDir}/generated/source/laboratory")

sourceSets {
  versionDir.mkdirs()
  getByName("main").java.srcDir(versionDir)
}

val generateVersion = tasks.register("pluginVersion") {
  inputs.property("version", version)
  outputs.dir(versionDir)

  doLast {
    val outputFile = file("$versionDir/io/mehow/laboratory/Config.kt")
    outputFile.parentFile.mkdirs()
    outputFile.writeText("""
      |package io.mehow.laboratory
      |
      |internal const val laboratoryVersion = "$version"
      |
    """.trimMargin("|"))
  }
}

tasks.withType<KotlinCompile> {
  dependsOn(generateVersion)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
