package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner

internal class LaboratoryPluginSpec : StringSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create().withPluginClasspath()
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

  "does not register feature flags factory for project without feature flags factory extension" {
    val fixture = "plugin-factory-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
        .withArguments("--stacktrace")
        .build()

    result.task(":generateFeatureFactory").shouldBeNull()
  }

  "fails for project without feature flags factory extension with feature flags factory argument" {
    val fixture = "plugin-factory-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
        .withArguments("generateFeatureFactory", "--stacktrace")
        .buildAndFail()

    result.task(":generateFeatureFactory").shouldBeNull()
  }

  "registers feature flags factory for project with feature flags factory extension" {
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

  "registers sourced storage for project with factory extension" {
    val fixture = "plugin-sourced-storage-present".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
        .withArguments("generateSourcedFeatureStorage", "--stacktrace")
        .build()

    result.task(":generateSourcedFeatureStorage").shouldNotBeNull()
  }

  "does not register feature flag sources factory for project without feature flag sources factory extension" {
    val fixture = "plugin-source-factory-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
        .withArguments("--stacktrace")
        .build()

    result.task(":generateFeatureSourceFactory").shouldBeNull()
  }

  "fails for project without feature sources factory extension with feature flag sources factory argument" {
    val fixture = "plugin-source-factory-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
        .withArguments("generateFeatureSourceFactory", "--stacktrace")
        .buildAndFail()

    result.task(":generateFeatureSourceFactory").shouldBeNull()
  }

  "registers feature flag sources factory for project with feature flag sources factory extension" {
    val fixture = "plugin-source-factory-present".toFixture()

    val result = gradleRunner.withProjectDir(fixture)
        .withArguments("generateFeatureSourceFactory", "--stacktrace")
        .build()

    result.task(":generateFeatureSourceFactory").shouldNotBeNull()
  }
})
