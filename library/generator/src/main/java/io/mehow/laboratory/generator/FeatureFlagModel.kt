package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.Nel
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.applicative.map
import arrow.core.extensions.fx
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.squareup.kotlinpoet.ClassName
import java.io.File

class FeatureFlagModel private constructor(
  internal val visibility: Visibility,
  internal val className: ClassName,
  internal val values: Nel<FeatureValue>,
  internal val source: FeatureFlagModel?,
) {
  internal val packageName = className.packageName
  internal val name = className.simpleName
  internal val fqcn = className.canonicalName

  fun generate(file: File): File {
    FeatureFlagGenerator(this).generate(file)
    val outputDir = file.toPath().resolve(packageName.replace(".", "/")).toFile()
    return File(outputDir, "$name.kt")
  }

  override fun equals(other: Any?) = other is FeatureFlagModel && fqcn == other.fqcn

  override fun hashCode() = fqcn.hashCode()

  override fun toString() = fqcn

  data class Builder(
    internal val visibility: Visibility,
    internal val packageName: String,
    internal val name: String,
    internal val values: List<FeatureValue>,
    internal val sourceValues: List<FeatureValue> = emptyList(),
  ) {
    internal val fqcn = if (packageName.isEmpty()) name else "$packageName.$name"

    fun build(): Either<GenerationFailure, FeatureFlagModel> {
      return Either.fx {
        val packageName = !validatePackageName()
        val name = !validateName()
        val values = !validateValues()
        val nestedSource = createNestedSource()?.bind()
        FeatureFlagModel(visibility, ClassName(packageName, name), values, nestedSource)
      }
    }

    private fun validatePackageName(): Either<GenerationFailure, String> {
      return Either.cond(
        test = packageName.isEmpty() || packageName.matches(packageNameRegex),
        ifTrue = { packageName },
        ifFalse = { InvalidPackageName(fqcn) }
      )
    }

    private fun validateName(): Either<GenerationFailure, String> {
      return Either.cond(
        test = name.matches(nameRegex),
        ifTrue = { name },
        ifFalse = { InvalidFeatureName(name, fqcn) }
      )
    }

    private fun validateValues(): Either<GenerationFailure, Nel<FeatureValue>> {
      return Nel.fromList(values)
        .toEither { NoFeatureValues(fqcn) }
        .flatMap { @Kt41142 validateValueNames(it) }
        .flatMap { @Kt41142 validateDuplicates(it) }
        .flatMap { @Kt41142 validateSingleFallback(it) }
    }

    private fun validateValueNames(values: Nel<FeatureValue>): Either<GenerationFailure, Nel<FeatureValue>> {
      val invalidNames = values.toList().map { @Kt41142 it.name }.filterNot(valueRegex::matches)
      return Nel.fromList(invalidNames)
        .toEither { values }
        .swap()
        .mapLeft { InvalidFeatureValues(it, fqcn) }
    }

    private fun validateDuplicates(values: Nel<FeatureValue>): Either<GenerationFailure, Nel<FeatureValue>> {
      val duplicates = values.toList().map { @Kt41142 it.name }.findDuplicates()
      return Nel.fromList(duplicates.toList())
        .toEither { values }
        .swap()
        .mapLeft { FeatureValuesCollision(it, fqcn) }
    }

    private fun validateSingleFallback(values: Nel<FeatureValue>): Either<GenerationFailure, Nel<FeatureValue>> {
      val fallbackProblems = values.toList()
        .filter { @Kt41142 it.isFallbackValue }
        .takeIf { it.size != 1 }
        ?.map { @Kt41142 it.name }
      return if (fallbackProblems == null) values.right()
      else Nel.fromList(fallbackProblems)
        .map { MultipleFeatureFallbackValues(it, fqcn) }
        .getOrElse { NoFeatureFallbackValue(fqcn) }
        .left()
    }

    private fun createNestedSource(): Either<GenerationFailure, FeatureFlagModel>? {
      return sourceValues
        .filterNot { it.name.equals("local", ignoreCase = true) }
        .takeIf { it.isNotEmpty() }
        ?.let { values ->
          val isFallbackValue = values.none(FeatureValue::isFallbackValue)
          return@let Builder(
            visibility = visibility,
            packageName = fqcn,
            name = "Source",
            values = Nel(FeatureValue("Local", isFallbackValue), values).toList(),
          )
        }?.build()
    }

    private companion object {
      val packageNameRegex = """^(?:[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)(?:\.[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)*${'$'}""".toRegex()
      val nameRegex = """^[a-zA-Z][a-zA-Z_\d]*""".toRegex()
      val valueRegex = """^[a-zA-Z][a-zA-Z_\d]*""".toRegex()
    }
  }
}

fun List<FeatureFlagModel.Builder>.buildAll(): Either<GenerationFailure, List<FeatureFlagModel>> {
  return traverse(Either.applicative()) { @Kt41142 it.build() }
    .map { listKind -> listKind.fix() }
    .flatMap { models -> models.checkForDuplicates { @Kt41142 FeaturesCollision.fromFeatures(it) } }
}

fun List<FeatureFlagModel>.sourceNames() = mapNotNull { @Kt41142 it.source }
  .map { @Kt41142 it.values }
  .flatMap { it.toList() }
  .map { @Kt41142 it.name }
