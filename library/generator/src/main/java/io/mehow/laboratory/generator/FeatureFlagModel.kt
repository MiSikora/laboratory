package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.Nel
import arrow.core.computations.either
import arrow.core.flatMap
import arrow.core.right
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.mehow.laboratory.generator.GenerationFailure.InvalidDefaultOption
import io.mehow.laboratory.generator.GenerationFailure.NoOption
import io.mehow.laboratory.generator.GenerationFailure.SelfSupervision
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
  @Deprecated("This method will be removed in 1.0.0. Use prepare instead.")
  public fun generate(file: File): File {
    prepare().writeTo(file)
    val outputDir = file.toPath().resolve(className.packageName.replace(".", "/")).toFile()
    return File(outputDir, "${className.simpleName}.kt")
  }

  public fun prepare(): FileSpec = FeatureFlagGenerator(this).fileSpec()

  override fun equals(other: Any?): Boolean =
    other is FeatureFlagModel && className.reflectionName() == other.className.reflectionName()

  override fun hashCode(): Int = className.reflectionName().hashCode()

  override fun toString(): String = className.toString()

  public data class Builder(
    internal val visibility: Visibility,
    internal val className: ClassName,
    internal val options: List<FeatureFlagOption>,
    internal val sourceOptions: List<FeatureFlagOption> = emptyList(),
    internal val description: String = "",
    internal val deprecation: Deprecation? = null,
    internal val supervisor: Supervisor.Builder? = null,
  ) {
    public fun build(): Either<GenerationFailure, FeatureFlagModel> {
      return either.eager {
        val options = validateOptions().bind()
        val nestedSource = createNestedSource()?.bind()
        val supervisor = validateSupervisor().bind()
        FeatureFlagModel(
            visibility = visibility,
            className = className,
            options = options,
            source = nestedSource,
            description = description,
            deprecation = deprecation,
            supervisor = supervisor,
        )
      }
    }

    private fun validateOptions(): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      return Nel.fromList(options)
          .toEither { NoOption(className.canonicalName) }
          .flatMap(::validateSingleDefault)
    }

    private fun validateSingleDefault(
      options: Nel<FeatureFlagOption>,
    ): Either<GenerationFailure, Nel<FeatureFlagOption>> {
      val defaultOptions = options.filter(FeatureFlagOption::isDefault).map(FeatureFlagOption::name)
      return Either.conditionally(
          defaultOptions.size == 1,
          ifTrue = { options },
          ifFalse = { InvalidDefaultOption(className.canonicalName, defaultOptions) }
      )
    }

    private fun createNestedSource(): Either<GenerationFailure, FeatureFlagModel>? {
      return sourceOptions
          .filterNot { it.name.equals("local", ignoreCase = true) }
          .takeIf { it.isNotEmpty() }
          ?.let { options ->
            val isDefaultValue = options.none(FeatureFlagOption::isDefault)

            return@let Builder(
                visibility = visibility,
                className = ClassName(className.packageName, className.simpleNames + "Source"),
                options = Nel(FeatureFlagOption("Local", isDefaultValue), options).toList(),
            )
          }?.build()
    }

    private fun validateSupervisor(): Either<GenerationFailure, Supervisor?> {
      return supervisor?.build()
          ?.flatMap(::validateSelfSupervision)
          ?: null.right()
    }

    private fun validateSelfSupervision(
      supervisor: Supervisor,
    ): Either<GenerationFailure, Supervisor> = Either.conditionally(
        test = supervisor.featureFlag.className.reflectionName() != className.reflectionName(),
        ifTrue = { supervisor },
        ifFalse = { SelfSupervision(supervisor.featureFlag.toString()) }
    )
  }
}

public fun List<FeatureFlagModel>.sourceNames(): List<String> = mapNotNull(FeatureFlagModel::source)
    .map(FeatureFlagModel::options)
    .flatMap { it.toList() }
    .map(FeatureFlagOption::name)

public fun List<FeatureFlagModel>.sourceModels(): List<FeatureFlagModel> =
  mapNotNull(FeatureFlagModel::source)
