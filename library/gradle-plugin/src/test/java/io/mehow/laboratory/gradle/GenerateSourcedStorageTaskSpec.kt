package io.mehow.laboratory.gradle

import arrow.core.NonEmptyList
import arrow.core.nel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mehow.laboratory.generator.FeatureValuesCollision
import io.mehow.laboratory.generator.FeaturesCollision
import io.mehow.laboratory.generator.InvalidFeatureName
import io.mehow.laboratory.generator.InvalidFeatureValues
import io.mehow.laboratory.generator.InvalidPackageName
import io.mehow.laboratory.generator.NoFeatureValues
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import java.io.File

class GenerateSourcedStorageTaskSpec : StringSpec({
  lateinit var gradleRunner: GradleRunner

  beforeTest {
    gradleRunner = GradleRunner.create()
      .withPluginClasspath()
      .withArguments("generateSourcedFeatureStorage", "--stacktrace")
  }

  afterTest {
    File("src/test/projects").getOutputDirs().forEach(File::cleanUpDir)
  }

  "generates storage with only local source" {
    val fixture = "sourced-storage-generate-local".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureStorage.Companion.sourcedGenerated(localSource: FeatureStorage): FeatureStorage
      |    = sourced(
      |  localSource,
      |  emptyMap()
      |)
    """.trimMargin("|")
  }

  "generates storage with sources" {
    val fixture = "sourced-storage-generate-sources".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureStorage.Companion.sourcedGenerated(
      |  localSource: FeatureStorage,
      |  remoteASource: FeatureStorage,
      |  remoteBSource: FeatureStorage
      |): FeatureStorage = sourced(
      |  localSource,
      |  mapOf(
      |    "RemoteA" to remoteASource,
      |    "RemoteB" to remoteBSource
      |  )
      |)
    """.trimMargin("|")
  }

  "uses implicit package name" {
    val fixture = "sourced-storage-package-name-implicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("io.mehow.implicit.sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.implicit"
  }

  "uses explicit package name" {
    val fixture = "sourced-storage-package-name-explicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("io.mehow.explicit.sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  "overrides implicit package name" {
    val fixture = "sourced-storage-package-name-explicit-override".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("io.mehow.explicit.sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  "generates internal storage" {
    val fixture = "sourced-storage-generate-internal".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain "internal fun FeatureStorage.Companion.sourcedGenerated(localSource: FeatureStorage)"
  }

  "generates public storage" {
    val fixture = "sourced-storage-generate-public".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    // Ensure public by checking a new line before enum declaration.
    // Change after https://github.com/square/kotlinpoet/pull/933
    factory.readText() shouldContain """
      |
      |fun FeatureStorage.Companion.sourcedGenerated(localSource: FeatureStorage)
    """.trimMargin("|")
  }

  "fails for corrupted storage package name" {
    val fixture = "sourced-storage-package-name-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidPackageName("!!!.sourcedGeneratedFeatureStorage").message

    val feature = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    feature.shouldNotExist()
  }

  "fails for features with no values" {
    val fixture = "sourced-storage-feature-values-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe FAILED
    result.output shouldContain NoFeatureValues("Feature").message

    val feature = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    feature.shouldNotExist()
  }

  "fails for features with colliding values" {
    val fixture = "sourced-storage-feature-values-colliding".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe FAILED
    result.output shouldContain FeatureValuesCollision("First".nel(), "Feature").message

    val feature = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    feature.shouldNotExist()
  }

  "fails for features with corrupted values" {
    val fixture = "sourced-storage-feature-values-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidFeatureValues(NonEmptyList("!!!, ???"), "Feature").message

    val feature = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    feature.shouldNotExist()
  }

  "fails for features with corrupted names" {
    val fixture = "sourced-storage-feature-name-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidFeatureName("!!!", "!!!").message

    val feature = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    feature.shouldNotExist()
  }

  "fails for features with corrupted package names" {
    val fixture = "sourced-storage-feature-package-name-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidPackageName("!!!.Feature").message

    val feature = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    feature.shouldNotExist()
  }

  "fails for features with colliding namespaces" {
    val fixture = "sourced-storage-feature-namespace-colliding".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe FAILED
    result.output shouldContain FeaturesCollision("io.mehow.Feature".nel()).message

    val feature = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    feature.shouldNotExist()
  }

  "generates storage with sourced from all modules" {
    val fixture = "sourced-storage-multimodule-generate-all".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureStorage.Companion.sourcedGenerated(
      |  localSource: FeatureStorage,
      |  remoteSource: FeatureStorage,
      |  remoteASource: FeatureStorage,
      |  remoteBSource: FeatureStorage
      |): FeatureStorage = sourced(
      |  localSource,
      |  mapOf(
      |    "Remote" to remoteSource,
      |    "RemoteA" to remoteASource,
      |    "RemoteB" to remoteBSource
      |  )
      |)
    """.trimMargin("|")
  }

  "generates storage with names from not excluded modules" {
    val fixture = "sourced-storage-multimodule-generate-filtered".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureStorage.Companion.sourcedGenerated(
      |  localSource: FeatureStorage,
      |  remoteSource: FeatureStorage,
      |  remoteBSource: FeatureStorage
      |): FeatureStorage = sourced(
      |  localSource,
      |  mapOf(
      |    "Remote" to remoteSource,
      |    "RemoteB" to remoteBSource
      |  )
      |)
    """.trimMargin("|")
  }

  "fails for features with colliding namespaces between modules" {
    val fixture = "sourced-storage-multimodule-namespace-colliding".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe FAILED
    result.output shouldContain FeaturesCollision("Feature".nel()).message

    val feature = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    feature.shouldNotExist()
  }

  "generates storage for Android project" {
    val fixture = "sourced-storage-android-smoke".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureStorage.Companion.sourcedGenerated(localSource: FeatureStorage): FeatureStorage
      |    = sourced(
      |  localSource,
      |  emptyMap()
      |)
    """.trimMargin("|")
  }

  "ignores any custom variant of local sources" {
    val fixture = "sourced-storage-generate-local-ignore".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("sourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureStorage.Companion.sourcedGenerated(localSource: FeatureStorage): FeatureStorage
      |    = sourced(
      |  localSource,
      |  emptyMap()
      |)
    """.trimMargin("|")
  }
})
