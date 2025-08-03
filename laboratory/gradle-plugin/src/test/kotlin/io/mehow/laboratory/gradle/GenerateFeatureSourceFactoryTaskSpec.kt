package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GenerateFeatureSourceFactoryTaskSpec : FunSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create()
      .withPluginClasspath()
      .withArguments("generateFeatureSourceFactory", "--stacktrace")
  }

  test("generates factory without any feature flags") {
    val fixture = "source-factory-generate-empty".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory = GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()
  }

  test("generates factory with feature flags") {
    val fixture = "source-factory-generate-feature-flags".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory = GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("io.mehow.first.FeatureA${"\${'$'}"}Source"),
      |    Class.forName("io.mehow.second.FeatureB${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }

  test("uses implicit package name") {
    val fixture = "source-factory-package-name-implicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("io.mehow.implicit.GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.implicit"
  }

  test("uses explicit package name") {
    val fixture = "source-factory-package-name-explicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("io.mehow.explicit.GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  test("overrides implicit package name") {
    val fixture = "source-factory-package-name-explicit-override".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("io.mehow.explicit.GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  test("generates internal factory") {
    val fixture = "source-factory-generate-internal".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "internal fun FeatureFactory.Companion.featureSourceGenerated()"
  }

  test("generates public factory") {
    val fixture = "source-factory-generate-public".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "public fun FeatureFactory.Companion.featureSourceGenerated()"
  }

  test("fails for feature flags with no options") {
    val fixture = "source-factory-feature-flag-option-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe FAILED
    result.output shouldContain "Feature must have at least one option"

    val feature = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    feature.shouldNotExist()
  }

  test("generates factory with feature flags from all modules") {
    val fixture = "source-factory-multi-module-generate-all".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory = GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureA${"\${'$'}"}Source"),
      |    Class.forName("FeatureB${"\${'$'}"}Source"),
      |    Class.forName("RootFeature${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }

  test("generates factory with feature flags only from included modules") {
    val fixture = "source-factory-multi-module-generate-filtered".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory = GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureB${"\${'$'}"}Source"),
      |    Class.forName("RootFeature${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }

  test("generates factory for Android project") {
    val fixture = "source-factory-android-smoke".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory = GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()
  }

  test("generates factory with supervised feature flag sources") {
    val fixture = "source-factory-generate-supervised-feature-flags".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory = GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("Child${"\${'$'}"}Source"),
      |    Class.forName("Grandparent${"\${'$'}"}Source"),
      |    Class.forName("Parent${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }
})
