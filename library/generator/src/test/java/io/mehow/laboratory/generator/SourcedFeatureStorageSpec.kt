package io.mehow.laboratory.generator

import arrow.core.identity
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
import java.util.Locale

class SourcedFeatureStorageSpec : DescribeSpec({
  val storageBuilder = SourcedFeatureStorageModel.Builder(
    visibility = Public,
    packageName = "io.mehow",
    sourceNames = listOf("Firebase", "S3"),
  )

  describe("sourced feature storage model") {
    context("package name") {
      it("can be empty") {
        val builder = storageBuilder.copy(packageName = "")

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
          val builder = storageBuilder.copy(packageName = packageName)

          val result = builder.build()

          result.shouldBeRight()
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { packageName ->
          val builder = storageBuilder.copy(packageName = packageName)

          val result = builder.build()

          result shouldBeLeft InvalidPackageName(builder.fqcn)
        }
      }
    }
  }

  describe("generated feature storage") {
    it("can be internal") {
      val tempDir = createTempDir()

      val outputFile = storageBuilder.copy(visibility = Internal)
        .build()
        .map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureStorage
            |import io.mehow.laboratory.FeatureStorage.Companion.sourced
            |import kotlin.collections.mapOf
            |import kotlin.to
            |
            |internal fun FeatureStorage.Companion.sourcedGenerated(
            |  localSource: FeatureStorage,
            |  firebaseSource: FeatureStorage,
            |  s3Source: FeatureStorage
            |): FeatureStorage = sourced(
            |  localSource,
            |  mapOf(
            |    "Firebase" to firebaseSource,
            |    "S3" to s3Source
            |  )
            |)
            |
        """.trimMargin()
      }
    }

    it("can be public") {
      val tempDir = createTempDir()

      val outputFile = storageBuilder.copy(visibility = Public)
        .build()
        .map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureStorage
            |import io.mehow.laboratory.FeatureStorage.Companion.sourced
            |import kotlin.collections.mapOf
            |import kotlin.to
            |
            |public fun FeatureStorage.Companion.sourcedGenerated(
            |  localSource: FeatureStorage,
            |  firebaseSource: FeatureStorage,
            |  s3Source: FeatureStorage
            |): FeatureStorage = sourced(
            |  localSource,
            |  mapOf(
            |    "Firebase" to firebaseSource,
            |    "S3" to s3Source
            |  )
            |)
            |
        """.trimMargin()
      }
    }

    it("ignores duplicate source names") {
      val tempDir = createTempDir()

      val outputFile = storageBuilder.copy(sourceNames = listOf("Foo", "Bar", "Baz", "Foo", "Baz", "Foo"))
        .build()
        .map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureStorage
            |import io.mehow.laboratory.FeatureStorage.Companion.sourced
            |import kotlin.collections.mapOf
            |import kotlin.to
            |
            |public fun FeatureStorage.Companion.sourcedGenerated(
            |  localSource: FeatureStorage,
            |  fooSource: FeatureStorage,
            |  barSource: FeatureStorage,
            |  bazSource: FeatureStorage
            |): FeatureStorage = sourced(
            |  localSource,
            |  mapOf(
            |    "Foo" to fooSource,
            |    "Bar" to barSource,
            |    "Baz" to bazSource
            |  )
            |)
            |
        """.trimMargin()
      }
    }

    it("ignores local source name") {
      val tempDir = createTempDir()

      val localPermutations = (0b00000..0b11111).map {
        listOf(it and 0b00001, it and 0b00010, it and 0b00100, it and 0b01000, it and 0b10000)
          .map { mask -> mask != 0 }
          .mapIndexed { index, mask ->
            val char = "local"[index].toString()
            if(mask) char else char.capitalize(Locale.ROOT)
          }.joinToString(separator = "")
      }

      val outputFile = storageBuilder.copy(sourceNames = localPermutations + "Foo")
        .build()
        .map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureStorage
            |import io.mehow.laboratory.FeatureStorage.Companion.sourced
            |import kotlin.collections.mapOf
            |import kotlin.to
            |
            |public fun FeatureStorage.Companion.sourcedGenerated(localSource: FeatureStorage,
            |    fooSource: FeatureStorage): FeatureStorage = sourced(
            |  localSource,
            |  mapOf(
            |    "Foo" to fooSource
            |  )
            |)
            |
        """.trimMargin()
      }
    }

    it("can have only local source") {
      val tempDir = createTempDir()

      val outputFile = storageBuilder.copy(sourceNames = emptyList())
        .build()
        .map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureStorage
            |import io.mehow.laboratory.FeatureStorage.Companion.sourced
            |import kotlin.collections.emptyMap
            |
            |public fun FeatureStorage.Companion.sourcedGenerated(localSource: FeatureStorage): FeatureStorage =
            |    sourced(
            |  localSource,
            |  emptyMap()
            |)
            |
        """.trimMargin()
      }
    }
  }
})
