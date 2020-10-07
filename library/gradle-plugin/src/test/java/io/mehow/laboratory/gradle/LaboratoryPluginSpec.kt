package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class LaboratoryPluginSpec : StringSpec({
  lateinit var gradleRunner: GradleRunner

  beforeTest {
    gradleRunner = GradleRunner.create().withPluginClasspath()
  }

  afterTest {
    File("src/test/projects").getOutputDirs().forEach(File::cleanUpDir)
  }

  "fails for project without Kotlin plugin" {
    val fixture = "plugin-kotlin-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("generateFeatureFlags", "--stacktrace")
      .buildAndFail()

    result.task(":generateFeatureFlags").shouldBeNull()
    result.output shouldContain "Laboratory Gradle plugin requires Kotlin plugin."
  }

  "registers feature flags task for project with Kotlin plugin" {
    val fixture = "plugin-kotlin-present".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("generateFeatureFlags", "--stacktrace")
      .build()

    result.task(":generateFeatureFlags").shouldNotBeNull()
  }

  "fails for Android project without Kotlin Android plugin" {
    val fixture = "plugin-kotlin-android-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("generateFeatureFlags", "--stacktrace")
      .buildAndFail()

    result.task(":generateFeatureFlags").shouldBeNull()
    result.output shouldContain "Laboratory Gradle plugin requires Kotlin plugin."
  }

  "registers feature flags task for project with Kotlin Android plugin" {
    val fixture = "plugin-kotlin-android-present".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("generateFeatureFlags", "--stacktrace")
      .build()

    result.task(":generateFeatureFlags").shouldNotBeNull()
  }

  "does not register feature factory for project without factory extension" {
    val fixture = "plugin-factory-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("--stacktrace")
      .build()

    result.task(":generateFeatureFactory").shouldBeNull()
  }

  "fails for project without factory extension with factory argument" {
    val fixture = "plugin-factory-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("generateFeatureFactory", "--stacktrace")
      .buildAndFail()

    result.task(":generateFeatureFactory").shouldBeNull()
  }

  "registers feature factory for project with factory extension" {
    val fixture = "plugin-factory-present".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("generateFeatureFactory", "--stacktrace")
      .build()

    result.task(":generateFeatureFactory").shouldNotBeNull()
  }

  "does not register sourced storage for project without sourced storage extension" {
    val fixture = "plugin-sourced-storage-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("--stacktrace")
      .build()

    result.task(":generateSourcedFeatureStorage").shouldBeNull()
  }

  "fails for project without sourced storage extension with factory argument" {
    val fixture = "plugin-sourced-storage-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("generateSourcedFeatureStorage", "--stacktrace")
      .buildAndFail()

    result.task(":generateSourcedFeatureStorage").shouldBeNull()
  }

  "registers ourced storage for project with factory extension" {
    val fixture = "plugin-sourced-storage-present".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
      .withArguments("generateSourcedFeatureStorage", "--stacktrace")
      .build()

    result.task(":generateSourcedFeatureStorage").shouldNotBeNull()
  }
})
