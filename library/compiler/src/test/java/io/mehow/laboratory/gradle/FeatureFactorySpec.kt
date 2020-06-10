package io.mehow.laboratory.gradle

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
import io.mehow.laboratory.compiler.FeatureFactoryModel
import io.mehow.laboratory.compiler.FeatureFlagModel
import io.mehow.laboratory.compiler.FlagNamespaceCollision
import io.mehow.laboratory.compiler.InvalidPackageName
import io.mehow.laboratory.compiler.Visiblity.Internal
import io.mehow.laboratory.compiler.Visiblity.Public

class FeatureFactorySpec : DescribeSpec({
  val featureA = FeatureFlagModel.Builder(
    visibility = Internal,
    packageName = "io.mehow",
    name = "FeatureA",
    values = listOf("First", "Second")
  ).build().getOrElse { error("Should be right") }

  val featureB = FeatureFlagModel.Builder(
    visibility = Internal,
    packageName = "io.mehow",
    name = "FeatureB",
    values = listOf("First", "Second")
  ).build().getOrElse { error("Should be right") }

  val featureC = FeatureFlagModel.Builder(
    visibility = Internal,
    packageName = "io.mehow.c",
    name = "FeatureA",
    values = listOf("First", "Second")
  ).build().getOrElse { error("Should be right") }

  val factoryBuilder = FeatureFactoryModel.Builder(
    visibility = Internal,
    packageName = "io.mehow",
    flags = listOf(featureA, featureB, featureC)
  )

  describe("feature factory model") {
    context("package name") {
      it("can be empty") {
        val builder = factoryBuilder.copy(packageName = "")

        val result = builder.build()

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

          val result = builder.build()

          result.shouldBeRight()
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { packageName ->
          val builder = factoryBuilder.copy(packageName = packageName)

          val result = builder.build()

          result shouldBeLeft InvalidPackageName(builder.fqcn)
        }
      }

      context("flags") {
        it("can be empty") {
          val builder = factoryBuilder.copy(flags = emptyList())

          val result = builder.build()

          result.shouldBeRight()
        }

        it("cannot have duplicates") {
          val builderA = factoryBuilder.copy(flags = listOf(featureA, featureA, featureB, featureC))
          val resultA = builderA.build()
          resultA shouldBeLeft FlagNamespaceCollision(featureA.nel())

          val builderB = factoryBuilder.copy(flags = listOf(featureA, featureB, featureB, featureC))
          val resultB = builderB.build()
          resultB shouldBeLeft FlagNamespaceCollision(featureB.nel())

          val builderC = factoryBuilder.copy(flags = listOf(featureA, featureB, featureC, featureC))
          val resultC = builderC.build()
          resultC shouldBeLeft FlagNamespaceCollision(featureC.nel())
        }

        it("can have unique features") {
          val result = factoryBuilder.build()

          result.shouldBeRight()
        }
      }
    }
  }

  describe("generated feature flag factory") {
    it("can be internal") {
      val tempDir = createTempDir()

      val outputFile = factoryBuilder.build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureFactory
            |import java.lang.Class
            |import kotlin.Enum
            |import kotlin.Suppress
            |import kotlin.collections.Set
            |import kotlin.collections.setOf
            |
            |internal fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
            |
            |private object GeneratedFeatureFactory : FeatureFactory {
            |  @Suppress("UNCHECKED_CAST")
            |  override fun create() = setOf(
            |    Class.forName("io.mehow.FeatureA"),
            |    Class.forName("io.mehow.FeatureB"),
            |    Class.forName("io.mehow.c.FeatureA")
            |  ) as Set<Class<Enum<*>>>
            |}
            |
          """.trimMargin("|")
      }
    }

    it("can be public") {
      val tempDir = createTempDir()
      val builder = factoryBuilder.copy(visibility = Public)

      val outputFile = builder.build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureFactory
            |import java.lang.Class
            |import kotlin.Enum
            |import kotlin.Suppress
            |import kotlin.collections.Set
            |import kotlin.collections.setOf
            |
            |fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
            |
            |private object GeneratedFeatureFactory : FeatureFactory {
            |  @Suppress("UNCHECKED_CAST")
            |  override fun create() = setOf(
            |    Class.forName("io.mehow.FeatureA"),
            |    Class.forName("io.mehow.FeatureB"),
            |    Class.forName("io.mehow.c.FeatureA")
            |  ) as Set<Class<Enum<*>>>
            |}
            |
          """.trimMargin("|")
      }
    }

    it("is optimized in case of no flags") {
      setOf(1).map {  }
      val tempDir = createTempDir()
      val builder = factoryBuilder.copy(flags = emptyList())

      val outputFile = builder.build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureFactory
            |import kotlin.collections.emptySet
            |
            |internal fun FeatureFactory.Companion.generated(): FeatureFactory = GeneratedFeatureFactory
            |
            |private object GeneratedFeatureFactory : FeatureFactory {
            |  override fun create() = emptySet()
            |}
            |
          """.trimMargin("|")
      }
    }
  }
})
