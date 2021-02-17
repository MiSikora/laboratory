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

@Suppress("LongParameterList") // All properties are required for code generation.
public class FeatureFlagModel private constructor(
  internal val visibility: Visibility,
  internal val className: ClassName,
  internal val options: Nel<FeatureFlagOption>,
  internal val source: FeatureFlagModel?,
  internal val description: String,
  internal val deprecation: Deprecation?,
  internal val supervisor: Supervisor?,
) {
  internal val packageName = className.packageName
  internal val name = className.simpleName
  internal val reflectionName = className.reflectionName()

  public fun generate(file: File): File {
    FeatureFlagGenerator(this).generate(file)
    val outputDir = file.toPath().resolve(packageName.replace(".", "/")).toFile()
    return File(outputDir, "$name.kt")
  }

  override fun equals(other: Any?): Boolean = other is FeatureFlagModel && reflectionName == other.reflectionName

  override fun hashCode(): Int = reflectionName.hashCode()

  override fun toString(): String = reflectionName

  public data class Builder(
    internal val visibility: Visibility,
    internal val packageName: String,
    internal val names: List<String>,
    internal val options: List<FeatureFlagOption>,
    internal val sourceOptions: List<FeatureFlagOption> = emptyList(),
    internal val description: String = "",
    internal val deprecation: Deprecation? = null,
    internal val supervisor: Supervisor.Builder? = null,
  ) {
    internal val fqcn = ClassName(packageName, names).canonicalName

    public fun build(): Either<GenerationFailure, FeatureFlagModel> {
      return Either.fx {
        val packageName = !validatePackageName()
        val names = !validateName()
        val options = !validateOptions()
        val nestedSource = createNestedSource()?.bind()
        val supervisor = !validateSupervisor()
        FeatureFlagModel(
            visibility = visibility,
            className = ClassName(packageName, names),
            options = options,
            source = nestedSource,
            description = description,
            deprecation = deprecation,
            supervisor = supervisor,
        )
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

    private fun validateOptions(): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      return Nel.fromList(options)
          .toEither { NoFeatureValues(fqcn) }
          .flatMap { @Kt41142 validateOptionNames(it) }
          .flatMap { @Kt41142 validateDuplicates(it) }
          .flatMap { @Kt41142 validateSingleDefault(it) }
    }

    private fun validateOptionNames(
      options: Nel<FeatureFlagOption>,
    ): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      val invalidOptions = options.toList().map { @Kt41142 it.name }.filterNot(optionRegex::matches)
      return Nel.fromList(invalidOptions)
          .toEither { options }
          .swap()
          .mapLeft { InvalidFeatureValues(it, fqcn) }
    }

    private fun validateDuplicates(
      options: Nel<FeatureFlagOption>,
    ): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      val duplicates = options.toList().map { @Kt41142 it.name }.findDuplicates()
      return Nel.fromList(duplicates.toList())
          .toEither { options }
          .swap()
          .mapLeft { FeatureValuesCollision(it, fqcn) }
    }

    private fun validateSingleDefault(
      options: Nel<FeatureFlagOption>,
    ): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      val defaultProblems = options.toList()
          .filter { @Kt41142 it.isDefault }
          .takeIf { it.size != 1 }
          ?.map { @Kt41142 it.name }
      return if (defaultProblems == null) options.right()
      else Nel.fromList(defaultProblems)
          .map { MultipleFeatureDefaultValues(it, fqcn) }
          .getOrElse { NoFeatureDefaultValue(fqcn) }
          .left()
    }

    private fun createNestedSource(): Either<GenerationFailure, FeatureFlagModel>? {
      return sourceOptions
          .filterNot { it.name.equals("local", ignoreCase = true) }
          .takeIf { it.isNotEmpty() }
          ?.let { options ->
            val isDefaultValue = options.none(FeatureFlagOption::isDefault)
            return@let Builder(
                visibility = visibility,
                packageName = packageName,
                names = names + "Source",
                options = Nel(FeatureFlagOption("Local", isDefaultValue), options).toList(),
            )
          }?.build()
    }

    private fun validateSupervisor(): Either<GenerationFailure, Supervisor?> = supervisor?.build()
        ?.flatMap { @Kt41142 validateSelfSupervision(it) }
        ?: Either.right(null)

    private fun validateSelfSupervision(
      supervisor: Supervisor,
    ): Either<GenerationFailure, Supervisor> = Either.cond(
        test = supervisor.featureFlag.className.canonicalName != fqcn,
        ifTrue = { supervisor },
        ifFalse = { SelfSupervision(supervisor.featureFlag.toString()) }
    )

    private companion object {
      val packageNameRegex = """^(?:[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)(?:\.[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)*${'$'}""".toRegex()
      val nameRegex = """^[a-zA-Z][a-zA-Z_\d]*""".toRegex()
      val optionRegex = """^[a-zA-Z][a-zA-Z_\d]*""".toRegex()
    }
  }
}

public fun List<FeatureFlagModel.Builder>.buildAll(): Either<GenerationFailure, List<FeatureFlagModel>> {
  return traverse(Either.applicative()) { @Kt41142 it.build() }
      .map { listKind -> listKind.fix() }
      .flatMap { models -> models.checkForDuplicates { @Kt41142 FeaturesCollision.fromFeatures(it) } }
}

public fun List<FeatureFlagModel>.sourceNames(): List<String> = mapNotNull { @Kt41142 it.source }
    .map { @Kt41142 it.options }
    .flatMap { it.toList() }
    .map { @Kt41142 it.name }

public fun List<FeatureFlagModel>.sourceModels(): List<FeatureFlagModel> = mapNotNull { @Kt41142 it.source }
