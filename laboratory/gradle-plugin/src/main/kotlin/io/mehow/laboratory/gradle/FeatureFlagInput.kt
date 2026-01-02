package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.Visibility
import java.io.Serializable

/**
 * Representation of a feature flag with multiple options. Can be either [MultiOption] or
 * [BinaryOption].
 */
public sealed class FeatureFlagInput(
  private val name: String,
  packageNameProvider: PackageNameProvider,
) : Serializable {
  /** Sets whether the generated feature flag should be public or internal. */
  public abstract var isPublic: Boolean

  /** Sets package name of the generated feature flag. Overwrites any previously set values. */
  public var packageName: String?
    get() = packageNameProvider.value
    set(value) = packageNameProvider.setValue(value)

  private val packageNameProvider = PackageNameProvider(packageNameProvider)

  /** Sets description of the generated feature flag. */
  public abstract var description: String?

  /** Sets a custom key that will be used for generated option factory. */
  public abstract var key: String?

  private val options = mutableMapOf<String, FeatureFlagOptionInput>()

  internal fun withOption(name: String, isDefault: Boolean) {
    val option = FeatureFlagOptionInput(name, isDefault)
    options += name to option
  }

  private val sources = mutableListOf<FeatureFlagOptionInput>()

  /**
   * Adds a feature flag source. Any sources that are named "Local", or any variation of this word,
   * will be filtered out.
   */
  public fun withSource(name: String): Unit = withSource(name, isDefault = false)

  /**
   * Adds a feature flag source that will be used a default source. Any sources that are named
   * "Local", or any variation of this word, will be filtered out. At most one value can be set with
   * this method.
   */
  public fun withDefaultSource(name: String): Unit = withSource(name, isDefault = true)

  private fun withSource(name: String, isDefault: Boolean) {
    val option = FeatureFlagOptionInput(name, isDefault)
    sources += option
  }

  private var deprecation: DeprecationInput? = null

  /** Annotates a feature flag as deprecated. */
  @JvmOverloads
  public fun deprecated(message: String, level: DeprecationLevel = DeprecationLevel.Warning) {
    deprecation = DeprecationInput(message, level.kotlinLevel)
  }

  internal fun toModel(): FeatureFlagModel =
    FeatureFlagModel(
      visibility = if (isPublic) Visibility.Public else Visibility.Internal,
      className = ClassName(packageName.orEmpty(), name),
      options = options.values.map(FeatureFlagOptionInput::toModel),
      sourceOptions = sources.map(FeatureFlagOptionInput::toModel),
      key = key,
      description = description.orEmpty(),
      deprecation = deprecation?.toModel(),
    )

  /**
   * Representation of a feature flag with multiple options. It must have at least one option and
   * exactly one default option.
   */
  public class MultiOption
  internal constructor(name: String, packageNameProvider: PackageNameProvider) :
    FeatureFlagInput(name, packageNameProvider) {
    override var isPublic: Boolean = true

    override var description: String? = null

    override var key: String? = null

    /** Adds a feature option. */
    public fun withOption(name: String): Unit = withOption(name, isDefault = false)

    /**
     * Adds a feature value that will be used as a default value. Exactly one value must be set with
     * this method.
     */
    public fun withDefaultOption(name: String): Unit = withOption(name, isDefault = true)

    internal companion object {
      private const val serialVersionUID = 1L
    }
  }

  /** Representation of a feature flag with only two options - "Enabled" and "Disabled". */
  public class BinaryOption
  internal constructor(name: String, isEnabled: Boolean, packageNameProvider: PackageNameProvider) :
    FeatureFlagInput(name, packageNameProvider) {
    override var isPublic: Boolean = true

    override var description: String? = null

    override var key: String? = null

    init {
      withOption("Enabled", isDefault = isEnabled)
      withOption("Disabled", isDefault = !isEnabled)
    }

    internal companion object {
      private const val serialVersionUID = 1L
    }
  }

  internal companion object {
    private const val serialVersionUID = 1L
  }
}
