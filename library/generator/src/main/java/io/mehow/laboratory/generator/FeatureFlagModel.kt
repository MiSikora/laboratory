package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.mehow.laboratory.generator.Visibility.Public

@Suppress("LongParameterList")
public class FeatureFlagModel private constructor(
  public val className: ClassName,
  public val options: List<FeatureFlagOption>,
  public val visibility: Visibility,
  public val key: String?,
  public val description: String,
  public val deprecation: Deprecation?,
  public val source: FeatureFlagModel?,
  public val supervisor: Supervisor?,
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
