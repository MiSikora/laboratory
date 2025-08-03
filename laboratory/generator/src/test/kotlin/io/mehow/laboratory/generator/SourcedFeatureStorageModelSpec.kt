package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.core.spec.style.FunSpec
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.generator.test.shouldSpecify
import java.util.Locale

class SourcedFeatureStorageModelSpec : FunSpec({
  test("can be internal") {
    val model = SourcedFeatureStorageModel(
      ClassName("io.mehow", "SourcedGeneratedFeatureStorage"),
      sourceNames = listOf("Firebase", "S3"),
      visibility = Internal,
    )

    val fileSpec = model.prepare()

    fileSpec shouldSpecify """
      |package io.mehow
      |
      |import io.mehow.laboratory.FeatureStorage
      |import io.mehow.laboratory.FeatureStorage.Companion.sourced
      |import kotlin.String
      |import kotlin.collections.Map
      |import kotlin.collections.emptyMap
      |import kotlin.collections.plus
      |import kotlin.to
      |
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): FirebaseStep = Builder(localSource, emptyMap())
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
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : FirebaseStep,
      |    S3Step,
      |    BuildingStep {
      |  override fun firebaseSource(source: FeatureStorage): S3Step = copy(
      |    remoteSources = remoteSources + ("Firebase" to source)
      |  )
      |
      |  override fun s3Source(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("S3" to source)
      |  )
      |
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
      |
    """.trimMargin()
  }

  test("can be public") {
    val model = SourcedFeatureStorageModel(
      ClassName("io.mehow", "SourcedGeneratedFeatureStorage"),
      sourceNames = listOf("Firebase", "S3"),
      visibility = Public,
    )

    val fileSpec = model.prepare()

    fileSpec shouldSpecify """
      |package io.mehow
      |
      |import io.mehow.laboratory.FeatureStorage
      |import io.mehow.laboratory.FeatureStorage.Companion.sourced
      |import kotlin.String
      |import kotlin.collections.Map
      |import kotlin.collections.emptyMap
      |import kotlin.collections.plus
      |import kotlin.to
      |
      |public fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): FirebaseStep = Builder(localSource, emptyMap())
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
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : FirebaseStep,
      |    S3Step,
      |    BuildingStep {
      |  override fun firebaseSource(source: FeatureStorage): S3Step = copy(
      |    remoteSources = remoteSources + ("Firebase" to source)
      |  )
      |
      |  override fun s3Source(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("S3" to source)
      |  )
      |
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
      |
    """.trimMargin()
  }

  test("ignores duplicate sources") {
    val model = SourcedFeatureStorageModel(
      ClassName("io.mehow", "SourcedGeneratedFeatureStorage"),
      sourceNames = listOf("Foo", "Bar", "Baz", "Foo", "Baz", "Foo"),
    )

    val fileSpec = model.prepare()

    fileSpec shouldSpecify """
      |package io.mehow
      |
      |import io.mehow.laboratory.FeatureStorage
      |import io.mehow.laboratory.FeatureStorage.Companion.sourced
      |import kotlin.String
      |import kotlin.collections.Map
      |import kotlin.collections.emptyMap
      |import kotlin.collections.plus
      |import kotlin.to
      |
      |internal fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BarStep = Builder(localSource, emptyMap())
      |
      |internal interface BarStep {
      |  public fun barSource(source: FeatureStorage): BazStep
      |}
      |
      |internal interface BazStep {
      |  public fun bazSource(source: FeatureStorage): FooStep
      |}
      |
      |internal interface FooStep {
      |  public fun fooSource(source: FeatureStorage): BuildingStep
      |}
      |
      |internal interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : BarStep,
      |    BazStep,
      |    FooStep,
      |    BuildingStep {
      |  override fun barSource(source: FeatureStorage): BazStep = copy(
      |    remoteSources = remoteSources + ("Bar" to source)
      |  )
      |
      |  override fun bazSource(source: FeatureStorage): FooStep = copy(
      |    remoteSources = remoteSources + ("Baz" to source)
      |  )
      |
      |  override fun fooSource(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("Foo" to source)
      |  )
      |
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
      |
    """.trimMargin()
  }

  test("ignores local source") {
    val localPermutations = (0b00000..0b11111).map {
      listOf(it and 0b00001, it and 0b00010, it and 0b00100, it and 0b01000, it and 0b10000)
        .map { mask -> mask != 0 }
        .mapIndexed { index, mask ->
          val chars = "local"[index].toString()
          if (mask) chars else chars.replaceFirstChar { char -> char.titlecase(Locale.ROOT) }
        }.joinToString(separator = "")
    }
    val model = SourcedFeatureStorageModel(
      ClassName("io.mehow", "SourcedGeneratedFeatureStorage"),
      sourceNames = localPermutations + "Foo",
      visibility = Public,
    )

    val fileSpec = model.prepare()

    fileSpec shouldSpecify """
      |package io.mehow
      |
      |import io.mehow.laboratory.FeatureStorage
      |import io.mehow.laboratory.FeatureStorage.Companion.sourced
      |import kotlin.String
      |import kotlin.collections.Map
      |import kotlin.collections.emptyMap
      |import kotlin.collections.plus
      |import kotlin.to
      |
      |public fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): FooStep = Builder(localSource, emptyMap())
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
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : FooStep,
      |    BuildingStep {
      |  override fun fooSource(source: FeatureStorage): BuildingStep = copy(
      |    remoteSources = remoteSources + ("Foo" to source)
      |  )
      |
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
      |
    """.trimMargin()
  }

  test("can have no external sources") {
    val model = SourcedFeatureStorageModel(
      ClassName("io.mehow", "SourcedGeneratedFeatureStorage"),
      sourceNames = emptyList(),
      visibility = Public,
    )

    val fileSpec = model.prepare()

    fileSpec shouldSpecify """
      |package io.mehow
      |
      |import io.mehow.laboratory.FeatureStorage
      |import io.mehow.laboratory.FeatureStorage.Companion.sourced
      |import kotlin.String
      |import kotlin.collections.Map
      |import kotlin.collections.emptyMap
      |
      |public fun FeatureStorage.Companion.sourcedBuilder(localSource: FeatureStorage): BuildingStep = Builder(localSource, emptyMap())
      |
      |public interface BuildingStep {
      |  public fun build(): FeatureStorage
      |}
      |
      |private data class Builder(
      |  private val localSource: FeatureStorage,
      |  private val remoteSources: Map<String, FeatureStorage>,
      |) : BuildingStep {
      |  override fun build(): FeatureStorage = sourced(localSource, remoteSources)
      |}
      |
    """.trimMargin()
  }
})
