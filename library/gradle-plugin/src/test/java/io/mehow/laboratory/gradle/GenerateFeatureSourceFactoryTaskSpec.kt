package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mehow.laboratory.generator.GenerationFailure.NoOption
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

internal class GenerateFeatureSourceFactoryTaskSpec : StringSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create()
        .withPluginClasspath()
        .withArguments("generateFeatureSourceFactory", "--stacktrace")
  }

  "generates factory without any feature flags" {
    val fixture = "source-factory-generate-empty".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory =
      |    GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  public override fun create() = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()
  }

  "generates factory with feature flags" {
    val fixture = "source-factory-generate-feature-flags".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory =
      |    GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  public override fun create() = setOf(
      |    Class.forName("io.mehow.first.FeatureA${"\${'$'}"}Source"),
      |    Class.forName("io.mehow.second.FeatureB${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }

  "uses implicit package name" {
    val fixture = "source-factory-package-name-implicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("io.mehow.implicit.GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.implicit"
  }

  "uses explicit package name" {
    val fixture = "source-factory-package-name-explicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("io.mehow.explicit.GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  "overrides implicit package name" {
    val fixture = "source-factory-package-name-explicit-override".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("io.mehow.explicit.GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  "generates internal factory" {
    val fixture = "source-factory-generate-internal".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "internal fun FeatureFactory.Companion.featureSourceGenerated()"
  }

  "generates public factory" {
    val fixture = "source-factory-generate-public".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain "public fun FeatureFactory.Companion.featureSourceGenerated()"
  }

  "fails for feature flags with no options" {
    val fixture = "source-factory-feature-flag-option-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe FAILED
    result.output shouldContain NoOption("Feature").message

    val feature = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    feature.shouldNotExist()
  }

  "generates factory with feature flags from all modules" {
    val fixture = "source-factory-multi-module-generate-all".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory =
      |    GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  public override fun create() = setOf(
      |    Class.forName("FeatureA${"\${'$'}"}Source"),
      |    Class.forName("FeatureB${"\${'$'}"}Source"),
      |    Class.forName("RootFeature${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }

  "generates factory with feature flags only from included modules" {
    val fixture = "source-factory-multi-module-generate-filtered".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory =
      |    GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  public override fun create() = setOf(
      |    Class.forName("FeatureB${"\${'$'}"}Source"),
      |    Class.forName("RootFeature${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }

  "generates factory for Android project" {
    val fixture = "source-factory-android-smoke".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory =
      |    GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  public override fun create() = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()
  }

  "generates factory with supervised feature flag sources" {
    val fixture = "source-factory-generate-supervised-feature-flags".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureSourceFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureSourceStorageFile("GeneratedFeatureSourceFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureSourceGenerated(): FeatureFactory =
      |    GeneratedFeatureSourceFactory
      |
      |private object GeneratedFeatureSourceFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  public override fun create() = setOf(
      |    Class.forName("Child${"\${'$'}"}Source"),
      |    Class.forName("Grandparent${"\${'$'}"}Source"),
      |    Class.forName("Parent${"\${'$'}"}Source")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }
})
