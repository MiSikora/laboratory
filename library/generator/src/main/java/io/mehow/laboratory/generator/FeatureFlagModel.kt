package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.mehow.laboratory.generator.Visibility.Public

public data class FeatureFlagModel private constructor(
  internal val className: ClassName,
  internal val options: List<FeatureFlagOption>,
  internal val visibility: Visibility,
  internal val key: String?,
  internal val description: String,
  internal val deprecation: Deprecation?,
  internal val source: FeatureFlagModel?,
  internal val supervisor: Supervisor?,
) {
  init {
    require(options.isNotEmpty()) {
      "${className.canonicalName} must have at least one option"
    }
    require(options.count(FeatureFlagOption::isDefault) == 1) {
      "${className.canonicalName} must have exactly one default option"
    }
    require(supervisor?.featureFlag != this) {
      "${className.canonicalName} cannot supervise itself"
    }
  }

  public constructor(
    className: ClassName,
    options: List<FeatureFlagOption>,
    visibility: Visibility = Public,
    key: String? = null,
    description: String = "",
    deprecation: Deprecation? = null,
    sourceOptions: List<FeatureFlagOption> = emptyList(),
    supervisor: Supervisor? = null,
  ) : this(
      className,
      options,
      visibility,
      key,
      description,
      deprecation,
      createSource(visibility, className, sourceOptions),
      supervisor,
  )

  public fun prepare(): FileSpec = FeatureFlagGenerator(this).fileSpec()

  override fun equals(other: Any?): Boolean =
    other is FeatureFlagModel && className.reflectionName() == other.className.reflectionName()

  override fun hashCode(): Int = className.reflectionName().hashCode()

  override fun toString(): String = className.canonicalName

  private companion object {
    fun createSource(
      visibility: Visibility,
      featureName: ClassName,
      options: List<FeatureFlagOption>,
    ) = options.toSourceOptions()?.let { sourceOptions ->
      FeatureFlagModel(featureName.toSourceName(), sourceOptions, visibility)
    }

    private fun ClassName.toSourceName() = ClassName(packageName, simpleNames + "Source")

    private fun List<FeatureFlagOption>.toSourceOptions() = filterNot { it.name.equals("local", ignoreCase = true) }
        .takeIf { it.isNotEmpty() }
        ?.let { options ->
          buildList {
            add(FeatureFlagOption("Local", isDefault = options.none(FeatureFlagOption::isDefault)))
            addAll(options)
          }
        }
  }
}

public fun List<FeatureFlagModel>.sourceNames(): List<String> = sourceModels()
    .map(FeatureFlagModel::options)
    .flatMap { it.toList() }
    .map(FeatureFlagOption::name)

public fun List<FeatureFlagModel>.sourceModels(): List<FeatureFlagModel> =
  mapNotNull(FeatureFlagModel::source)
