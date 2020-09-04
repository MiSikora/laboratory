package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.Nel
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.applicative.map
import arrow.core.extensions.fx
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.core.flatMap
import java.io.File

class FeatureFlagModel private constructor(
  internal val visibility: Visibility,
  internal val packageName: String,
  internal val name: String,
  internal val values: Nel<String>,
) {
  val fqcn = if (packageName.isEmpty()) name else "$packageName.$name"

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
    internal val values: List<String>,
  ) {
    internal val fqcn = if (packageName.isEmpty()) name else "$packageName.$name"

    fun build(): Either<GenerationFailure, FeatureFlagModel> {
      return Either.fx {
        val packageName = !validatePackageName()
        val name = !validateName()
        val values = !validateValues()
        FeatureFlagModel(visibility, packageName, name, values)
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

    private fun validateValues(): Either<GenerationFailure, Nel<String>> {
      return Nel.fromList(values)
        .toEither { NoFeatureValues(fqcn) }
        .flatMap { @Kt41142 validateValueNames(it) }
        .flatMap { @Kt41142 validateDuplicates(it) }
    }

    private fun validateValueNames(values: Nel<String>): Either<GenerationFailure, Nel<String>> {
      val invalidNames = values.toList().filterNot(valueRegex::matches)
      return Nel.fromList(invalidNames)
        .toEither { values }
        .swap()
        .mapLeft { InvalidFeatureValues(it, fqcn) }
    }

    private fun validateDuplicates(values: Nel<String>): Either<GenerationFailure, Nel<String>> {
      val duplicates = values.toList().findDuplicates()
      return Nel.fromList(duplicates.toList())
        .toEither { values }
        .swap()
        .mapLeft { FeatureValuesCollision(it, fqcn) }
    }

    internal companion object {
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
