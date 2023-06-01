package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

internal class GenerateFeatureFactoryTaskSpec : StringSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create()
        .withPluginClasspath()
        .withArguments("generateFeatureFactory", "--stacktrace")
  }

  "generates factory without any feature flags" {
    val fixture = "factory-generate-empty".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureFactoryFile("GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureGenerated(): FeatureFactory = GeneratedFeatureFactory
      |
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()
  }

  "generates factory with feature flags" {
    val fixture = "factory-generate-features".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureFactoryFile("GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureGenerated(): FeatureFactory = GeneratedFeatureFactory
      |
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("io.mehow.first.FeatureA"),
      |    Class.forName("io.mehow.second.FeatureB")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }

  "uses implicit package name" {
    val fixture = "factory-package-name-implicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureFactoryFile("io.mehow.implicit.GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.implicit"
  }

  "uses explicit package name" {
    val fixture = "factory-package-name-explicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureFactoryFile("io.mehow.explicit.GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  "overrides implicit package name" {
    val fixture = "factory-package-name-explicit-override".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureFactoryFile("io.mehow.explicit.GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  "generates internal factory" {
    val fixture = "factory-generate-internal".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureFactoryFile("GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain "internal fun FeatureFactory.Companion.featureGenerated()"
  }

  "generates public factory" {
    val fixture = "factory-generate-public".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.featureFactoryFile("GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain "public fun FeatureFactory.Companion.featureGenerated()"
  }

  "fails for feature flags with no options" {
    val fixture = "factory-feature-flag-option-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFactory")!!.outcome shouldBe FAILED
    result.output shouldContain "Feature must have at least one option"

    fixture.featureFactoryFile("GeneratedFeatureFactory").shouldNotExist()
  }

  "generates factory with feature flags from all modules" {
    val fixture = "factory-multi-module-generate-all".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureFactoryFile("GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureGenerated(): FeatureFactory = GeneratedFeatureFactory
      |
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureA"),
      |    Class.forName("FeatureB"),
      |    Class.forName("RootFeature")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }

  "generates factory with feature flags only from included modules" {
    val fixture = "factory-multi-module-generate-filtered".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureFactoryFile("GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureGenerated(): FeatureFactory = GeneratedFeatureFactory
      |
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("FeatureB"),
      |    Class.forName("RootFeature")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }

  "generates factory for Android project" {
    val fixture = "factory-android-smoke".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureFactoryFile("GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureGenerated(): FeatureFactory = GeneratedFeatureFactory
      |
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
      |}
    """.trimMargin()
  }

  "generates factory with supervised feature flags" {
    val fixture = "factory-generate-supervised-feature-flags".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.featureFactoryFile("GeneratedFeatureFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun FeatureFactory.Companion.featureGenerated(): FeatureFactory = GeneratedFeatureFactory
      |
      |private object GeneratedFeatureFactory : FeatureFactory {
      |  @Suppress("UNCHECKED_CAST")
      |  override fun create(): Set<Class<out Feature<*>>> = setOf(
      |    Class.forName("Child"),
      |    Class.forName("Grandparent"),
      |    Class.forName("Parent")
      |  ) as Set<Class<out Feature<*>>>
      |}
    """.trimMargin()
  }
})
