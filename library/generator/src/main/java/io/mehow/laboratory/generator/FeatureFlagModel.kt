package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.Nel
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.core.traverseEither
import com.squareup.kotlinpoet.ClassName
import java.io.File

@Suppress("LongParameterList") // All properties are required for code generation.
public class FeatureFlagModel internal constructor(
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

    public fun build(): Either<GenerationFailure, FeatureFlagModel> = either.eager {
      val packageName = validatePackageName().bind()
      val names = validateName().bind()
      val options = validateOptions().bind()
      val nestedSource = createNestedSource()?.bind()
      val supervisor = validateSupervisor().bind()
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

    private fun validatePackageName() = Either.conditionally(
        packageName.isEmpty() || packageName.matches(packageNameRegex),
        { InvalidPackageName(fqcn) },
        { packageName },
    )

    private fun validateName() = names.traverseEither { name ->
      Either.conditionally(
          name.matches(nameRegex),
          { InvalidFeatureName(name, fqcn) },
          { name },
      )
    }

    private fun validateOptions(): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      return Nel.fromList(options)
          .toEither { NoFeatureValues(fqcn) }
          .flatMap(::validateOptionNames)
          .flatMap(::validateDuplicates)
          .flatMap(::validateSingleDefault)
    }

    private fun validateOptionNames(
      options: Nel<FeatureFlagOption>,
    ): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      val invalidOptions = options.toList().map(FeatureFlagOption::name).filterNot(optionRegex::matches)
      return Nel.fromList(invalidOptions)
          .toEither { options }
          .swap()
          .mapLeft { InvalidFeatureValues(it, fqcn) }
    }

    private fun validateDuplicates(
      options: Nel<FeatureFlagOption>,
    ): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      val duplicates = options.toList().map(FeatureFlagOption::name).findDuplicates()
      return Nel.fromList(duplicates.toList())
          .toEither { options }
          .swap()
          .mapLeft { FeatureValuesCollision(it, fqcn) }
    }

    private fun validateSingleDefault(
      options: Nel<FeatureFlagOption>,
    ): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      val defaultProblems = options.toList()
          .filter(FeatureFlagOption::isDefault)
          .takeIf { it.size != 1 }
          ?.map(FeatureFlagOption::name)
      return if (defaultProblems == null) options.right()
      else Nel.fromList(defaultProblems)
          .map { MultipleFeatureDefaultValues(it, fqcn) }
          .getOrElse { NoFeatureDefaultValue(fqcn) }
          .left()
    }

    private fun createNestedSource() = sourceOptions
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

    private fun validateSupervisor() = supervisor?.build()
        ?.flatMap(::validateSelfSupervision)
        ?: null.right()

    private fun validateSelfSupervision(supervisor: Supervisor) = Either.conditionally(
        supervisor.featureFlag.className.canonicalName != fqcn,
        { SelfSupervision(supervisor.featureFlag.toString()) },
        { supervisor },
    )

    private companion object {
      val packageNameRegex = """^(?:[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)(?:\.[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)*${'$'}""".toRegex()
      val nameRegex = """^[a-zA-Z][a-zA-Z_\d]*""".toRegex()
      val optionRegex = """^[a-zA-Z][a-zA-Z_\d]*""".toRegex()
    }
  }
}

public fun List<FeatureFlagModel.Builder>.buildAll(): Either<GenerationFailure, List<FeatureFlagModel>> =
  traverseEither(FeatureFlagModel.Builder::build).flatMap { models ->
    models.checkForDuplicates(FeaturesCollision::fromFeatures)
  }

public fun List<FeatureFlagModel>.sourceNames(): List<String> = mapNotNull(FeatureFlagModel::source)
    .map(FeatureFlagModel::options)
    .flatMap { it.toList() }
    .map(FeatureFlagOption::name)

public fun List<FeatureFlagModel>.sourceModels(): List<FeatureFlagModel> = mapNotNull(FeatureFlagModel::source)
