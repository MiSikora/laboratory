package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GenerateOptionFactoryTaskSpec : FunSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create()
      .withPluginClasspath()
      .withArguments("generateOptionFactory", "--stacktrace")
  }

  test("generates factory without any feature flags") {
    val fixture = "option-factory-generate-empty".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateOptionFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
      |
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = null
      |}
    """.trimMargin()
  }

  test("generates factory using feature flag fqcns") {
    val fixture = "option-factory-generate-fqcn".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateOptionFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
      |
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = when (key) {
      |    "io.mehow.first.FeatureA" -> when (name) {
      |      "FirstA" -> FeatureA.FirstA
      |      "SecondA" -> FeatureA.SecondA
      |      else -> null
      |    }
      |    "io.mehow.second.FeatureB" -> when (name) {
      |      "FirstB" -> FeatureB.FirstB
      |      else -> null
      |    }
      |    else -> null
      |  }
      |}
    """.trimMargin()
  }

  test("generates factory using feature flag keys") {
    val fixture = "option-factory-generate-key".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateOptionFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
      |
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = when (key) {
      |    "Key A" -> when (name) {
      |      "FirstA" -> FeatureA.FirstA
      |      "SecondA" -> FeatureA.SecondA
      |      else -> null
      |    }
      |    "Key B" -> when (name) {
      |      "FirstB" -> FeatureB.FirstB
      |      else -> null
      |    }
      |    else -> null
      |  }
      |}
    """.trimMargin()
  }

  test("fails to generate factory for feature flags with duplicate keys") {
    val fixture = "option-factory-duplicate-key".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateOptionFactory")!!.outcome shouldBe FAILED
    result.output shouldContain """
      |Feature flags must have unique keys. Found following duplicates:
      | - Some Key: [io.mehow.first.FeatureA, io.mehow.second.FeatureB]
    """.trimMargin()

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldNotExist()
  }

  test("fails to generate factory for key duplicating another fqcn") {
    val fixture = "option-factory-duplicate-key-fqcn".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateOptionFactory")!!.outcome shouldBe FAILED
    result.output shouldContain """
      |Feature flags must have unique keys. Found following duplicates:
      | - io.mehow.first.FeatureA: [io.mehow.first.FeatureA, io.mehow.second.FeatureB]
    """.trimMargin()

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldNotExist()
  }

  test("fails to generate factory for feature flags with no options") {
    val fixture = "option-factory-no-option".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateOptionFactory")!!.outcome shouldBe FAILED
    result.output shouldContain "Feature must have at least one option"

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldNotExist()
  }

  test("uses implicit package name") {
    val fixture = "option-factory-package-name-implicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.optionFactoryFile("io.mehow.implicit.GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.implicit"
  }

  test("uses explicit package name") {
    val fixture = "option-factory-package-name-explicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.optionFactoryFile("io.mehow.explicit.GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  test("overrides implicit package name") {
    val fixture = "option-factory-package-name-explicit-override".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.optionFactoryFile("io.mehow.explicit.GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain "package io.mehow.explicit"
  }

  test("generates internal factory") {
    val fixture = "option-factory-generate-internal".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain "internal fun OptionFactory.Companion.generated()"
  }

  test("generates public factory") {
    val fixture = "option-factory-generate-public".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain "public fun OptionFactory.Companion.generated()"
  }

  test("generates factory with feature flags from all modules") {
    val fixture = "option-factory-multi-module-generate-all".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateOptionFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
      |
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = when (key) {
      |    "Key A" -> when (name) {
      |      "First" -> FeatureA.First
      |      else -> null
      |    }
      |    "Key B" -> when (name) {
      |      "First" -> FeatureB.First
      |      else -> null
      |    }
      |    "Key Root" -> when (name) {
      |      "First" -> RootFeature.First
      |      else -> null
      |    }
      |    else -> null
      |  }
      |}
    """.trimMargin()
  }

  test("generates factory with feature flags only from included modules") {
    val fixture = "option-factory-multi-module-generate-filtered".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateOptionFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
      |
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = when (key) {
      |    "Key B" -> when (name) {
      |      "First" -> FeatureB.First
      |      else -> null
      |    }
      |    "Key Root" -> when (name) {
      |      "First" -> RootFeature.First
      |      else -> null
      |    }
      |    else -> null
      |  }
      |}
    """.trimMargin()
  }

  test("generates factory for Android project") {
    val fixture = "option-factory-android-smoke".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateOptionFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory", isAndroid = true)
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
      |
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = null
      |}
    """.trimMargin()
  }

  test("fails to generate factory for feature flags with duplicate keys in different modules") {
    val fixture = "option-factory-multi-module-duplicate-key".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateOptionFactory")!!.outcome shouldBe FAILED
    result.output shouldContain """
      |Feature flags must have unique keys. Found following duplicates:
      | - Some Key: [FeatureA, FeatureB]
    """.trimMargin()

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldNotExist()
  }

  test("generates factory for feature flags with duplicate keys in filtered modules") {
    val fixture = "option-factory-multi-module-filtered-duplicate-key".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateOptionFactory")!!.outcome shouldBe SUCCESS

    val factory = fixture.optionFactoryFile("GeneratedOptionFactory")
    factory.shouldExist()

    factory.readText() shouldContain """
      |fun OptionFactory.Companion.generated(): OptionFactory = GeneratedOptionFactory
      |
      |private object GeneratedOptionFactory : OptionFactory {
      |  override fun create(key: String, name: String): Feature<*>? = when (key) {
      |    "Key Root" -> when (name) {
      |      "First" -> RootFeature.First
      |      else -> null
      |    }
      |    "Some Key" -> when (name) {
      |      "First" -> FeatureB.First
      |      else -> null
      |    }
      |    else -> null
      |  }
      |}
    """.trimMargin()
  }
})
