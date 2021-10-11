package io.mehow.laboratory.gradle

import arrow.core.getOrHandle
import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.Deprecation
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.FeatureFlagOption
import io.mehow.laboratory.generator.Supervisor
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.gradle.DeprecationLevel.Warning
import org.gradle.api.Action
import org.gradle.api.GradleException

/**
 * Representation of a generated feature flag. It must have at least one value and exactly one default value.
 */
public class FeatureFlagInput internal constructor(
  private val name: String,
  private val packageNameProvider: () -> String,
  private val supervisor: (() -> Supervisor.Builder)? = null,
) {
  /**
   * Sets whether the generated feature flag should be public or internal.
   */
  public var isPublic: Boolean = true

  /**
   * Sets package name of the generated feature flag. Overwrites any previously set values.
   */
  public var packageName: String? = null

  /**
   * Sets description of the generated feature flag.
   */
  public var description: String? = null

  /**
   * Sets a custom key that will be used for generated option factory.
   */
  public var key: String? = null

  private val options: MutableList<FeatureFlagOption> = mutableListOf()

  /**
   * Adds a feature option.
   */
  public fun withOption(name: String): Unit = withOption(name) { }

  /**
   * Adds a feature option and configures features flags supervised by it.
   */
  public fun withOption(name: String, action: Action<ChildFeatureFlagsInput>): Unit =
    withOption(name, isDefault = false, action)

  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'withOption()' instead.",
      replaceWith = ReplaceWith("withOption(value)"),
  )
  public fun withValue(value: String): Unit = withOption(value)

  /**
   * Adds a feature value that will be used as a default value.
   * Exactly one value must be set with this method.
   */
  public fun withDefaultOption(name: String): Unit = withDefaultOption(name) { }

  /**
   * Adds a feature value that will be used as a default value and configures features flags supervised by it.
   * Exactly one value must be set with this method.
   */
  public fun withDefaultOption(name: String, action: Action<ChildFeatureFlagsInput>): Unit =
    withOption(name, isDefault = true, action)

  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'withDefaultOption()' instead.",
      replaceWith = ReplaceWith("withDefaultOption(value)"),
  )
  public fun withDefaultValue(value: String): Unit = withDefaultOption(value)

  private val childFeatureInputs = mutableListOf<ChildFeatureFlagsInput>()

  private fun withOption(name: String, isDefault: Boolean, action: Action<ChildFeatureFlagsInput>) {
    val option = FeatureFlagOption(name, isDefault)
    options += option
    val packageNameProvider = { packageName ?: packageNameProvider() }
    val supervisorBuilder = {
      val supervisor = toBuilder().build().getOrHandle { throw GradleException(it.message) }
      Supervisor.Builder(supervisor, option)
    }
    childFeatureInputs += ChildFeatureFlagsInput(packageNameProvider, supervisorBuilder).let { input ->
      action.execute(input)
      return@let input
    }
  }

  private val sources: MutableList<FeatureFlagOption> = mutableListOf()

  /**
   * Adds a feature flag source. Any sources that are named "Local", or any variation of this word,
   * will be filtered out.
   */
  public fun withSource(name: String) {
    sources += FeatureFlagOption(name)
  }

  /**
   * Adds a feature flag source that will be used a default source. Any sources that are named "Local",
   * or any variation of this word, will be filtered out.
   * At most one value can be set with this method.
   */
  public fun withDefaultSource(name: String) {
    sources += FeatureFlagOption(name, isDefault = true)
  }

  private var deprecation: Deprecation? = null

  /**
   * Annotates a feature flag as deprecated.
   */
  @JvmOverloads public fun deprecated(message: String, level: DeprecationLevel = Warning) {
    deprecation = Deprecation(message, level.kotlinLevel)
  }

  private fun toBuilder() = FeatureFlagModel.Builder(
      visibility = if (isPublic) Public else Internal,
      className = ClassName(packageName ?: packageNameProvider(), name),
      options = options,
      sourceOptions = sources,
      key = key,
      description = description.orEmpty(),
      deprecation = deprecation,
      supervisor = supervisor?.invoke()?.build()?.getOrHandle { throw GradleException(it.message) },
  )

  internal fun toBuilders(): List<FeatureFlagModel.Builder> =
    listOf(toBuilder()) + childFeatureInputs.flatMap(ChildFeatureFlagsInput::toBuilders)
}
