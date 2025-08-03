package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class DependencyContributionSpec : FunSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create()
      .withPluginClasspath()
      .withArguments(
        "generateFeatureFactory",
        "generateSourcedFeatureStorage",
        "generateOptionFactory",
        "generateFeatureSourceFactory",
        "--stacktrace",
      )
  }

  test("generates only feature factory") {
    val fixture = "dependency-contribution-feature-factory".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateOptionFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe TaskOutcome.SUCCESS

    fixture.featureFactoryFile("GeneratedFeatureFactory").readText() shouldContain """
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureA")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()

    fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory").readText() shouldContain """
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()

    fixture.optionFactoryFile("GeneratedOptionFactory").readText() shouldContain """
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = null
      |}
    """.trimMargin()

    fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage").readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BuildingStep = Builder(localSource, emptyMap())
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : BuildingStep {
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  test("generates only feature source factory") {
    val fixture = "dependency-contribution-feature-source-factory".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateOptionFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe TaskOutcome.SUCCESS

    fixture.featureFactoryFile("GeneratedFeatureFactory").readText() shouldContain """
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()

    fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory").readText() shouldContain """
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureA${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()

    fixture.optionFactoryFile("GeneratedOptionFactory").readText() shouldContain """
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = null
      |}
    """.trimMargin()

    fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage").readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BuildingStep = Builder(localSource, emptyMap())
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : BuildingStep {
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  test("generates only option factory") {
    val fixture = "dependency-contribution-option-factory".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateOptionFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe TaskOutcome.SUCCESS

    fixture.featureFactoryFile("GeneratedFeatureFactory").readText() shouldContain """
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()

    fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory").readText() shouldContain """
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()

    fixture.optionFactoryFile("GeneratedOptionFactory").readText() shouldContain """
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = when (key) {
      |    "FeatureA" -> when (name) {
      |      "First" -> FeatureA.First
      |      else -> null
      |    }
      |    else -> null
      |  }
      |}
    """.trimMargin()

    fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage").readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BuildingStep = Builder(localSource, emptyMap())
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : BuildingStep {
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  test("generates only sourced storage") {
    val fixture = "dependency-contribution-sourced-storage".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateOptionFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe TaskOutcome.SUCCESS

    fixture.featureFactoryFile("GeneratedFeatureFactory").readText() shouldContain """
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()

    fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory").readText() shouldContain """
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()

    fixture.optionFactoryFile("GeneratedOptionFactory").readText() shouldContain """
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = null
      |}
    """.trimMargin()

    fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage").readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): RemoteStep = Builder(localSource, emptyMap())
      |
      |internal interface RemoteStep {
      |  public fun remoteSource(source: FeatureStorage): BuildingStep
      |}
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : RemoteStep,
      |    BuildingStep {
      |  override fun remoteSource(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("Remote" to source)
      |  )
      |
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  test("generates all contributed features explicitly") {
    val fixture = "dependency-contribution-all-explicit".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateOptionFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe TaskOutcome.SUCCESS

    fixture.featureFactoryFile("GeneratedFeatureFactory").readText() shouldContain """
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureA")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()

    fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory").readText() shouldContain """
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureA${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()

    fixture.optionFactoryFile("GeneratedOptionFactory").readText() shouldContain """
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = when (key) {
      |    "FeatureA" -> when (name) {
      |      "First" -> FeatureA.First
      |      else -> null
      |    }
      |    else -> null
      |  }
      |}
    """.trimMargin()

    fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage").readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): RemoteStep = Builder(localSource, emptyMap())
      |
      |internal interface RemoteStep {
      |  public fun remoteSource(source: FeatureStorage): BuildingStep
      |}
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : RemoteStep,
      |    BuildingStep {
      |  override fun remoteSource(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("Remote" to source)
      |  )
      |
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  test("generates all contributed features implicitly") {
    val fixture = "dependency-contribution-all-implicit".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateOptionFactory")!!.outcome shouldBe TaskOutcome.SUCCESS
    result.task(":generateSourcedFeatureStorage")!!.outcome shouldBe TaskOutcome.SUCCESS

    fixture.featureFactoryFile("GeneratedFeatureFactory").readText() shouldContain """
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureA")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()

    fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory").readText() shouldContain """
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureA${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()

    fixture.optionFactoryFile("GeneratedOptionFactory").readText() shouldContain """
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = when (key) {
      |    "FeatureA" -> when (name) {
      |      "First" -> FeatureA.First
      |      else -> null
      |    }
      |    else -> null
      |  }
      |}
    """.trimMargin()

    fixture.sourcedStorageFile("SourcedGeneratedFeatureStorage").readText() shouldContain """
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): RemoteStep = Builder(localSource, emptyMap())
      |
      |internal interface RemoteStep {
      |  public fun remoteSource(source: FeatureStorage): BuildingStep
      |}
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : RemoteStep,
      |    BuildingStep {
      |  override fun remoteSource(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("Remote" to source)
      |  )
      |
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
    """.trimMargin()
  }

  test("fails to depend on a project with no contributions") {
    val fixture = "dependency-contribution-none".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    @Suppress("MaxLineLength")
    result.output shouldContain "Dependency in project 'dependency-contribution-none' on 'feature' must have at least one contribution"
  }
})
