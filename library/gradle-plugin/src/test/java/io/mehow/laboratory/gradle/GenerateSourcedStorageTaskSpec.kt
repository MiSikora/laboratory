package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

internal class GenerateSourcedStorageTaskSpec : StringSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create()
        .withPluginClasspath()
        .withArguments("generateSourcedFeatureStorage", "--stacktrace")
  }

  "generates storage with only local source" {
    val fixture = "sourced-storage-generate-local".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BuildingStep =
      |    Builder(localSource, emptyMap())
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : BuildingStep {
      |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
      |
    """.trimMargin()
  }

  "generates storage with sources" {
    val fixture = "sourced-storage-generate-sources".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): RemoteAStep =
      |    Builder(localSource, emptyMap())
      |
      |internal interface RemoteAStep {
      |  public fun remoteASource(source: FeatureStorage): RemoteBStep
      |}
      |
      |internal interface RemoteBStep {
      |  public fun remoteBSource(source: FeatureStorage): BuildingStep
      |}
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : RemoteAStep, RemoteBStep, BuildingStep {
      |  public override fun remoteASource(source: FeatureStorage): RemoteBStep = copy(
      |    remoteSources = remoteSources + ("RemoteA" to source)
      |  )
      |
      |  public override fun remoteBSource(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("RemoteB" to source)
      |  )
      |
      |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  "uses implicit package name" {
    val fixture = "sourced-storage-package-name-implicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("io.mehow.implicit.SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.implicit"
  }

  "uses explicit package name" {
    val fixture = "sourced-storage-package-name-explicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("io.mehow.explicit.SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  "overrides implicit package name" {
    val fixture = "sourced-storage-package-name-explicit-override".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("io.mehow.explicit.SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  "generates internal storage" {
    val fixture = "sourced-storage-generate-internal".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain "internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage)"
  }

  "generates public storage" {
    val fixture = "sourced-storage-generate-public".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain "public fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage)"
  }

  "fails for feature flags with no options" {
    val fixture = "sourced-storage-feature-flag-option-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe FAILED
    result.output shouldContain "Feature must have at least one option"

    val feature = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    feature.shouldNotExist()
  }

  "generates storage with sourced from all modules" {
    val fixture = "sourced-storage-multi-module-generate-all".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): RemoteStep =
      |    Builder(localSource, emptyMap())
      |
      |internal interface RemoteStep {
      |  public fun remoteSource(source: FeatureStorage): RemoteAStep
      |}
      |
      |internal interface RemoteAStep {
      |  public fun remoteASource(source: FeatureStorage): RemoteBStep
      |}
      |
      |internal interface RemoteBStep {
      |  public fun remoteBSource(source: FeatureStorage): BuildingStep
      |}
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : RemoteStep, RemoteAStep, RemoteBStep, BuildingStep {
      |  public override fun remoteSource(source: FeatureStorage): RemoteAStep = copy(
      |    remoteSources = remoteSources + ("Remote" to source)
      |  )
      |
      |  public override fun remoteASource(source: FeatureStorage): RemoteBStep = copy(
      |    remoteSources = remoteSources + ("RemoteA" to source)
      |  )
      |
      |  public override fun remoteBSource(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("RemoteB" to source)
      |  )
      |
      |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  "generates storage with names only from included modules" {
    val fixture = "sourced-storage-multi-module-generate-filtered".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): RemoteStep =
      |    Builder(localSource, emptyMap())
      |
      |internal interface RemoteStep {
      |  public fun remoteSource(source: FeatureStorage): RemoteBStep
      |}
      |
      |internal interface RemoteBStep {
      |  public fun remoteBSource(source: FeatureStorage): BuildingStep
      |}
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : RemoteStep, RemoteBStep, BuildingStep {
      |  public override fun remoteSource(source: FeatureStorage): RemoteBStep = copy(
      |    remoteSources = remoteSources + ("Remote" to source)
      |  )
      |
      |  public override fun remoteBSource(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("RemoteB" to source)
      |  )
      |
      |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  "generates storage for Android project" {
    val fixture = "sourced-storage-android-smoke".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BuildingStep =
      |    Builder(localSource, emptyMap())
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : BuildingStep {
      |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  "ignores any custom variant of local sources" {
    val fixture = "sourced-storage-generate-local-ignore".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BuildingStep =
      |    Builder(localSource, emptyMap())
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : BuildingStep {
      |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  "generates storage with supervised feature flag sources" {
    val fixture = "sourced-storage-generate-supervised-feature-flags".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe SUCCESS

    val factory = fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage")
    factory.shouldExist()

    factory.readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): ChildStep =
      |    Builder(localSource, emptyMap())
      |
      |internal interface ChildStep {
      |  public fun childSource(source: FeatureStorage): GrandparentStep
      |}
      |
      |internal interface GrandparentStep {
      |  public fun grandparentSource(source: FeatureStorage): ParentStep
      |}
      |
      |internal interface ParentStep {
      |  public fun parentSource(source: FeatureStorage): BuildingStep
      |}
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : ChildStep, GrandparentStep, ParentStep, BuildingStep {
      |  public override fun childSource(source: FeatureStorage): GrandparentStep = copy(
      |    remoteSources = remoteSources + ("Child" to source)
      |  )
      |
      |  public override fun grandparentSource(source: FeatureStorage): ParentStep = copy(
      |    remoteSources = remoteSources + ("Grandparent" to source)
      |  )
      |
      |  public override fun parentSource(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("Parent" to source)
      |  )
      |
      |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }
})
