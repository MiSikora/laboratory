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
  internal val reflectionName = className.reflectionName()

  fun generate(file: File): File {
    FeatureFlagGenerator(this).generate(file)
    val outputDir = file.toPath().resolve(packageName.replace(".", "/")).toFile()
    return File(outputDir, "$name.kt")
  }

  override fun equals(other: Any?) = other is FeatureFlagModel && reflectionName == other.reflectionName

  override fun hashCode() = reflectionName.hashCode()

  override fun toString() = reflectionName

  data class Builder(
    internal val visibility: Visibility,
    internal val packageName: String,
    internal val names: List<String>,
    internal val values: List<FeatureValue>,
    internal val sourceValues: List<FeatureValue> = emptyList(),
  ) {
    internal val fqcn = ClassName(packageName, names).canonicalName

    fun build(): Either<GenerationFailure, FeatureFlagModel> {
      return Either.fx {
        val packageName = !validatePackageName()
        val names = !validateName()
        val values = !validateValues()
        val nestedSource = createNestedSource()?.bind()
        FeatureFlagModel(visibility, ClassName(packageName, names), values, nestedSource)
      }
    }

    private fun validatePackageName(): Either<GenerationFailure, String> {
      return Either.cond(
        test = packageName.isEmpty() || packageName.matches(packageNameRegex),
        ifTrue = { packageName },
        ifFalse = { InvalidPackageName(fqcn) }
      )
    }

    private fun validateName(): Either<GenerationFailure, List<String>> {
      return names.traverse(Either.applicative()) { name ->
        Either.cond(
          test = name.matches(nameRegex),
          ifTrue = { name },
          ifFalse = { InvalidFeatureName(name, fqcn) }
        )
      }.map { listKind -> listKind.fix() }
    }

    private fun validateValues(): Either<GenerationFailure, Nel<FeatureValue>> {
      return Nel.fromList(values)
        .toEither { NoFeatureValues(fqcn) }
        .flatMap { @Kt41142 validateValueNames(it) }
        .flatMap { @Kt41142 validateDuplicates(it) }
        .flatMap { @Kt41142 validateSingleDefault(it) }
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

    private fun validateSingleDefault(values: Nel<FeatureValue>): Either<GenerationFailure, Nel<FeatureValue>> {
      val defaultProblems = values.toList()
        .filter { @Kt41142 it.isDefaultValue }
        .takeIf { it.size != 1 }
        ?.map { @Kt41142 it.name }
      return if (defaultProblems == null) values.right()
      else Nel.fromList(defaultProblems)
        .map { MultipleFeatureDefaultValues(it, fqcn) }
        .getOrElse { NoFeatureDefaultValue(fqcn) }
        .left()
    }

    private fun createNestedSource(): Either<GenerationFailure, FeatureFlagModel>? {
      return sourceValues
        .filterNot { it.name.equals("local", ignoreCase = true) }
        .takeIf { it.isNotEmpty() }
        ?.let { values ->
          val isDefaultValue = values.none(FeatureValue::isDefaultValue)
          return@let Builder(
            visibility = visibility,
            packageName = packageName,
            names = names + "Source",
            values = Nel(FeatureValue("Local", isDefaultValue), values).toList(),
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

fun List<FeatureFlagModel>.sourceModels() = mapNotNull { @Kt41142 it.source }
