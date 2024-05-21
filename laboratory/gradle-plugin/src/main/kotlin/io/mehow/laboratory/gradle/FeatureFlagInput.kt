package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.Visibility
import org.gradle.api.Action
import java.io.Serializable

/**
 * Representation of a generated feature flag. It must have at least one value and exactly one default value.
 */
public class FeatureFlagInput internal constructor(
  private val name: String,
  packageNameProvider: PackageNameProvider,
  private val supervisor: SupervisorInput?,
) : Serializable {
  /**
   * Sets whether the generated feature flag should be public or internal.
   */
  public var isPublic: Boolean = true

  /**
   * Sets package name of the generated feature flag. Overwrites any previously set values.
   */
  public var packageName: String?
    get() = packageNameProvider.value
    set(value) = packageNameProvider.setValue(value)

  private val packageNameProvider = PackageNameProvider(packageNameProvider)

  /**
   * Sets description of the generated feature flag.
   */
  public var description: String? = null

  /**
   * Sets a custom key that will be used for generated option factory.
   */
  public var key: String? = null

  private val options = mutableListOf<FeatureFlagOptionInput>()

  private val childFeatureInputs = mutableListOf<ChildFeatureFlagsInput>()

  /**
   * Adds a feature option.
   */
  public fun withOption(name: String): Unit = withOption(name, action = {})

  /**
   * Adds a feature option and configures features flags supervised by it.
   */
  public fun withOption(
    name: String,
    action: Action<ChildFeatureFlagsInput>,
  ): Unit = withOption(name, isDefault = false, action = action)

  /**
   * Adds a feature value that will be used as a default value.
   * Exactly one value must be set with this method.
   */
  public fun withDefaultOption(name: String): Unit = withDefaultOption(name, action = {})

  /**
   * Adds a feature value that will be used as a default value and configures features flags supervised by it.
   * Exactly one value must be set with this method.
   */
  public fun withDefaultOption(
    name: String,
    action: Action<ChildFeatureFlagsInput>,
  ): Unit = withOption(name, isDefault = true, action = action)

  private fun withOption(
    name: String,
    isDefault: Boolean,
    action: Action<ChildFeatureFlagsInput>,
  ) {
    val option = FeatureFlagOptionInput(name, isDefault)
    val supervisor = SupervisorInput(this, option)
    childFeatureInputs += ChildFeatureFlagsInput(packageNameProvider, supervisor).apply(action::execute)
    options += option
  }

  private val sources = mutableListOf<FeatureFlagOptionInput>()

  /**
   * Adds a feature flag source. Any sources that are named "Local", or any variation of this word,
   * will be filtered out.
   */
  public fun withSource(name: String): Unit = withSource(name, isDefault = false)

  /**
   * Adds a feature flag source that will be used a default source. Any sources that are named "Local",
   * or any variation of this word, will be filtered out.
   * At most one value can be set with this method.
   */
  public fun withDefaultSource(name: String): Unit = withSource(name, isDefault = true)

  private fun withSource(
    name: String,
    isDefault: Boolean,
  ) {
    val option = FeatureFlagOptionInput(name, isDefault)
    sources += option
  }

  private var deprecation: DeprecationInput? = null

  /**
   * Annotates a feature flag as deprecated.
   */
  @JvmOverloads public fun deprecated(
    message: String,
    level: DeprecationLevel = DeprecationLevel.Warning,
  ) {
    deprecation = DeprecationInput(message, level.kotlinLevel)
  }

  internal fun toModel() = FeatureFlagModel(
    visibility = if (isPublic) Visibility.Public else Visibility.Internal,
    className = ClassName(packageNameProvider.value.orEmpty(), name),
    options = options.map(FeatureFlagOptionInput::toModel),
    sourceOptions = sources.map(FeatureFlagOptionInput::toModel),
    key = key,
    description = description.orEmpty(),
    deprecation = deprecation?.toModel(),
    supervisor = supervisor?.toModel(),
  )

  internal fun toModels() = listOf(toModel()) + childFeatureInputs.flatMap(ChildFeatureFlagsInput::toModels)

  internal companion object {
    private const val serialVersionUID = 0L
  }
}
