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
import kotlin.io.path.createTempDirectory

internal class SourcedFeatureStorageGeneratorSpec : DescribeSpec({
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
      val tempDir = createTempDirectory().toFile()

      val outputFile = storageBuilder.copy(visibility = Internal)
          .build()
          .map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureStorage
            |import io.mehow.laboratory.FeatureStorage.Companion.sourced
            |import kotlin.Deprecated
            |import kotlin.String
            |import kotlin.collections.Map
            |import kotlin.collections.emptyMap
            |import kotlin.collections.mapOf
            |import kotlin.collections.plus
            |import kotlin.to
            |
            |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): FirebaseStep =
            |    Builder(localSource, emptyMap())
            |
            |internal interface FirebaseStep {
            |  public fun firebaseSource(source: FeatureStorage): S3Step
            |}
            |
            |internal interface S3Step {
            |  public fun s3Source(source: FeatureStorage): BuildingStep
            |}
            |
            |internal interface BuildingStep {
            |  public fun build(): FeatureStorage
            |}
            |
            |private data class Builder(
            |  private val localSource: FeatureStorage,
            |  private val remoteSources: Map<String, FeatureStorage>
            |) : FirebaseStep, S3Step, BuildingStep {
            |  public override fun firebaseSource(source: FeatureStorage): S3Step = copy(
            |    remoteSources = remoteSources + ("Firebase" to source)
            |  )
            |
            |  public override fun s3Source(source: FeatureStorage): BuildingStep = copy(
            |    remoteSources = remoteSources + ("S3" to source)
            |  )
            |
            |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
            |}
            |
            |@Deprecated("This method will be removed in 1.0.0. Use sourcedBuilder instead.")
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
      val tempDir = createTempDirectory().toFile()

      val outputFile = storageBuilder.copy(visibility = Public)
          .build()
          .map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureStorage
            |import io.mehow.laboratory.FeatureStorage.Companion.sourced
            |import kotlin.Deprecated
            |import kotlin.String
            |import kotlin.collections.Map
            |import kotlin.collections.emptyMap
            |import kotlin.collections.mapOf
            |import kotlin.collections.plus
            |import kotlin.to
            |
            |public fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): FirebaseStep =
            |    Builder(localSource, emptyMap())
            |
            |public interface FirebaseStep {
            |  public fun firebaseSource(source: FeatureStorage): S3Step
            |}
            |
            |public interface S3Step {
            |  public fun s3Source(source: FeatureStorage): BuildingStep
            |}
            |
            |public interface BuildingStep {
            |  public fun build(): FeatureStorage
            |}
            |
            |private data class Builder(
            |  private val localSource: FeatureStorage,
            |  private val remoteSources: Map<String, FeatureStorage>
            |) : FirebaseStep, S3Step, BuildingStep {
            |  public override fun firebaseSource(source: FeatureStorage): S3Step = copy(
            |    remoteSources = remoteSources + ("Firebase" to source)
            |  )
            |
            |  public override fun s3Source(source: FeatureStorage): BuildingStep = copy(
            |    remoteSources = remoteSources + ("S3" to source)
            |  )
            |
            |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
            |}
            |
            |@Deprecated("This method will be removed in 1.0.0. Use sourcedBuilder instead.")
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
      val tempDir = createTempDirectory().toFile()

      val outputFile = storageBuilder.copy(sourceNames = listOf("Foo", "Bar", "Baz", "Foo", "Baz", "Foo"))
          .build()
          .map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureStorage
            |import io.mehow.laboratory.FeatureStorage.Companion.sourced
            |import kotlin.Deprecated
            |import kotlin.String
            |import kotlin.collections.Map
            |import kotlin.collections.emptyMap
            |import kotlin.collections.mapOf
            |import kotlin.collections.plus
            |import kotlin.to
            |
            |public fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BarStep =
            |    Builder(localSource, emptyMap())
            |
            |public interface BarStep {
            |  public fun barSource(source: FeatureStorage): BazStep
            |}
            |
            |public interface BazStep {
            |  public fun bazSource(source: FeatureStorage): FooStep
            |}
            |
            |public interface FooStep {
            |  public fun fooSource(source: FeatureStorage): BuildingStep
            |}
            |
            |public interface BuildingStep {
            |  public fun build(): FeatureStorage
            |}
            |
            |private data class Builder(
            |  private val localSource: FeatureStorage,
            |  private val remoteSources: Map<String, FeatureStorage>
            |) : BarStep, BazStep, FooStep, BuildingStep {
            |  public override fun barSource(source: FeatureStorage): BazStep = copy(
            |    remoteSources = remoteSources + ("Bar" to source)
            |  )
            |
            |  public override fun bazSource(source: FeatureStorage): FooStep = copy(
            |    remoteSources = remoteSources + ("Baz" to source)
            |  )
            |
            |  public override fun fooSource(source: FeatureStorage): BuildingStep = copy(
            |    remoteSources = remoteSources + ("Foo" to source)
            |  )
            |
            |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
            |}
            |
            |@Deprecated("This method will be removed in 1.0.0. Use sourcedBuilder instead.")
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
      val tempDir = createTempDirectory().toFile()

      val localPermutations = (0b00000..0b11111).map {
        listOf(it and 0b00001, it and 0b00010, it and 0b00100, it and 0b01000, it and 0b10000)
            .map { mask -> mask != 0 }
            .mapIndexed { index, mask ->
              val char = "local"[index].toString()
              if (mask) char else char.capitalize(Locale.ROOT)
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
            |import kotlin.Deprecated
            |import kotlin.String
            |import kotlin.collections.Map
            |import kotlin.collections.emptyMap
            |import kotlin.collections.mapOf
            |import kotlin.collections.plus
            |import kotlin.to
            |
            |public fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): FooStep =
            |    Builder(localSource, emptyMap())
            |
            |public interface FooStep {
            |  public fun fooSource(source: FeatureStorage): BuildingStep
            |}
            |
            |public interface BuildingStep {
            |  public fun build(): FeatureStorage
            |}
            |
            |private data class Builder(
            |  private val localSource: FeatureStorage,
            |  private val remoteSources: Map<String, FeatureStorage>
            |) : FooStep, BuildingStep {
            |  public override fun fooSource(source: FeatureStorage): BuildingStep = copy(
            |    remoteSources = remoteSources + ("Foo" to source)
            |  )
            |
            |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
            |}
            |
            |@Deprecated("This method will be removed in 1.0.0. Use sourcedBuilder instead.")
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
      val tempDir = createTempDirectory().toFile()

      val outputFile = storageBuilder.copy(sourceNames = emptyList())
          .build()
          .map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.FeatureStorage
            |import io.mehow.laboratory.FeatureStorage.Companion.sourced
            |import kotlin.Deprecated
            |import kotlin.String
            |import kotlin.collections.Map
            |import kotlin.collections.emptyMap
            |
            |public fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BuildingStep =
            |    Builder(localSource, emptyMap())
            |
            |public interface BuildingStep {
            |  public fun build(): FeatureStorage
            |}
            |
            |private data class Builder(
            |  private val localSource: FeatureStorage,
            |  private val remoteSources: Map<String, FeatureStorage>
            |) : BuildingStep {
            |  public override fun build(): FeatureStorage = sourced(localSource, remoteSources)
            |}
            |
            |@Deprecated("This method will be removed in 1.0.0. Use sourcedBuilder instead.")
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
