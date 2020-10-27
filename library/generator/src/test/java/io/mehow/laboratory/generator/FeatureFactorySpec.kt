package io.mehow.laboratory.generator

import arrow.core.getOrElse
import arrow.core.identity
import arrow.core.nel
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

internal class FeatureFactorySpec : DescribeSpec({
  val featureA = FeatureFlagModel.Builder(
      visibility = Internal,
      packageName = "io.mehow",
      names = listOf("FeatureA"),
      values = listOf(FeatureValue("First", isDefaultValue = true), FeatureValue("Second")),
  ).build().getOrElse { error("Should be right") }

  val featureB = FeatureFlagModel.Builder(
      visibility = Internal,
      packageName = "io.mehow",
      names = listOf("FeatureB"),
      values = listOf(FeatureValue("First", isDefaultValue = true), FeatureValue("Second")),
  ).build().getOrElse { error("Should be right") }

  val featureC = FeatureFlagModel.Builder(
      visibility = Internal,
      packageName = "io.mehow.c",
      names = listOf("FeatureA"),
      values = listOf(FeatureValue("First", isDefaultValue = true), FeatureValue("Second")),
  ).build().getOrElse { error("Should be right") }

  val factoryBuilder = FeatureFactoryModel.Builder(
      visibility = Internal,
      packageName = "io.mehow",
      features = listOf(featureA, featureB, featureC),
  )

  describe("feature factory model") {
    context("package name") {
      it("can be empty") {
        val builder = factoryBuilder.copy(packageName = "")

        val result = builder.build("GeneratedFeatureFactory")

        result.shouldBeRight()
      }

      it("can be valid java package name") {
        val packagePart = Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")
        val packageCount = Arb.int(1..10)
        checkAll(packagePart, packageCount) { part, count ->
          val packageName = List(count, ::identity).joinToString(".") {
            part.take(1) + part.drop(1).toList().shuffled().joinToString("")
          }
          val builder = factoryBuilder.copy(packageName = packageName)

          val result = builder.build("GeneratedFeatureFactory")

          result.shouldBeRight()
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { packageName ->
          val builder = factoryBuilder.copy(packageName = packageName)

          val result = builder.build("GeneratedFeatureFactory")

          result shouldBeLeft InvalidPackageName("${packageName}.GeneratedFeatureFactory")
        }
      }
    }

    context("name") {
      it("cannot be blank") {
        checkAll(Arb.stringPattern("([ ]{0,10})")) { name ->
          val result = factoryBuilder.build(name)

          result shouldBeLeft InvalidFactoryName(name, "${factoryBuilder.packageName}.${name}")
        }
      }

      it("cannot start with an underscore") {
        checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
          val result = factoryBuilder.build(name)

          result shouldBeLeft InvalidFactoryName(name, "${factoryBuilder.packageName}.${name}")
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
          val result = factoryBuilder.build(name)

          result shouldBeLeft InvalidFactoryName(name, "${factoryBuilder.packageName}.${name}")
        }
      }

      it("can contain alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
          val result = factoryBuilder.build(name)

          result.shouldBeRight()
        }
      }
    }

    context("values") {
      it("can be empty") {
        val builder = factoryBuilder.copy(features = emptyList())

        val result = builder.build("GeneratedFeatureFactory")

        result.shouldBeRight()
      }

      it("cannot have duplicates") {
        val builderA = factoryBuilder.copy(
            features = listOf(featureA, featureA, featureB, featureC)
        )
        val resultA = builderA.build("GeneratedFeatureFactory")
        resultA shouldBeLeft FeaturesCollision(featureA.reflectionName.nel())

        val builderB = factoryBuilder.copy(
            features = listOf(featureA, featureB, featureB, featureC)
        )
        val resultB = builderB.build("GeneratedFeatureFactory")
        resultB shouldBeLeft FeaturesCollision(featureB.reflectionName.nel())

        val builderC = factoryBuilder.copy(
            features = listOf(featureA, featureB, featureC, featureC)
        )
        val resultC = builderC.build("GeneratedFeatureFactory")
        resultC shouldBeLeft FeaturesCollision(featureC.reflectionName.nel())
      }

      it("can have unique features") {
        val result = factoryBuilder.build("GeneratedFeatureFactory")

        result.shouldBeRight()
      }
    }
  }

  describe("generated feature flag factory") {
    it("can be internal") {
      val tempDir = createTempDir()

      val outputFile = factoryBuilder
          .build("GeneratedFeatureFactory")
          .map { model -> model.generate("generated", tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import io.mehow.laboratory.FeatureFactory
            |import java.lang.Class
            |import kotlin.Suppress
            |import kotlin.collections.Set
            |import kotlin.collections.setOf
            |
            |internal fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
            |
            |private object GeneratedFeatureFactory : FeatureFactory {
            |  @Suppress("UNCHECKED_CAST")
            |  public override fun create() = setOf(
            |    Class.forName("io.mehow.FeatureA"),
            |    Class.forName("io.mehow.FeatureB"),
            |    Class.forName("io.mehow.c.FeatureA")
            |  ) as Set<Class<Feature<*>>>
            |}
            |
          """.trimMargin("|")
      }
    }

    it("can be public") {
      val tempDir = createTempDir()
      val builder = factoryBuilder.copy(visibility = Public)

      val outputFile = builder
          .build("GeneratedFeatureFactory")
          .map { model -> model.generate("generated", tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import io.mehow.laboratory.FeatureFactory
            |import java.lang.Class
            |import kotlin.Suppress
            |import kotlin.collections.Set
            |import kotlin.collections.setOf
            |
            |public fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
            |
            |private object GeneratedFeatureFactory : FeatureFactory {
            |  @Suppress("UNCHECKED_CAST")
            |  public override fun create() = setOf(
            |    Class.forName("io.mehow.FeatureA"),
            |    Class.forName("io.mehow.FeatureB"),
            |    Class.forName("io.mehow.c.FeatureA")
            |  ) as Set<Class<Feature<*>>>
            |}
            |
          """.trimMargin("|")
      }
    }

    it("is optimized in case of no features") {
      setOf(1).map { }
      val tempDir = createTempDir()
      val builder = factoryBuilder.copy(features = emptyList())

      val outputFile = builder
          .build("GeneratedFeatureFactory")
          .map { model -> model.generate("generated", tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import io.mehow.laboratory.FeatureFactory
            |import java.lang.Class
            |import kotlin.collections.emptySet
            |
            |internal fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
            |
            |private object GeneratedFeatureFactory : FeatureFactory {
            |  public override fun create() = emptySet<Class<Feature<*>>>()
            |}
            |
          """.trimMargin("|")
      }
    }
  }
})
