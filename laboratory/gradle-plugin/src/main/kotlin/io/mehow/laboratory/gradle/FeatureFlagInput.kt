package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.FeatureFlagTrueOption
import io.mehow.laboratory.generator.Visibility
import java.io.Serializable

/** Base configuration type for a feature flag definition. */
@LaboratoryDsl
public sealed class FeatureFlagInput(
  private val name: String,
  packageNameProvider: PackageNameProvider,
) : Serializable {
  /** Overrides the package name for the generated feature flag. */
  public var packageName: String?
    get() = packageNameProvider.value
    set(value) = packageNameProvider.setValue(value)

  private val packageNameProvider = PackageNameProvider(packageNameProvider)

  /** External key used to identify the feature flag in remote systems or option factories. */
  public abstract var key: String?

  /** Marks the feature flag as remotely sourced. */
  public abstract var isRemote: Boolean

  /** Sets feature flag default value to be remotely sourced. */
  public abstract var isRemoteValueDefault: Boolean

  /** Human-readable description of the feature flag. */
  public abstract var description: String?

  /** Controls whether the generated feature flag is public or internal. */
  public abstract var isPublic: Boolean

  private val options = mutableMapOf<String, FeatureFlagOptionInput>()

  internal fun withOption(name: String, isDefault: Boolean) {
    val option = FeatureFlagOptionInput(name, isDefault)
    options += name to option
  }

  private var deprecation: DeprecationInput? = null

  /** Marks the feature flag as deprecated. */
  @JvmOverloads
  public fun deprecated(message: String, level: DeprecationLevel = DeprecationLevel.Warning) {
    deprecation = DeprecationInput(message, level.kotlinLevel)
  }

  internal fun toModel(): FeatureFlagModel =
    FeatureFlagModel(
      className = ClassName(packageName.orEmpty(), name),
      options = options.values.map(FeatureFlagOptionInput::toModel),
      visibility = if (isPublic) Visibility.Public else Visibility.Internal,
      isRemote = isRemote,
      isRemoteValueDefault = isRemoteValueDefault,
      description = description.orEmpty(),
      deprecation = deprecation?.toModel(),
      key = key,
      trueOption =
        when (this) {
          is BinaryOption -> FeatureFlagTrueOption("Enabled")
          is MultiOption -> null
        },
    )

  /**
   * Feature flag with multiple selectable options. Exactly one option must be marked as the
   * default.
   */
  @LaboratoryDsl
  public class MultiOption
  internal constructor(name: String, packageNameProvider: PackageNameProvider) :
    FeatureFlagInput(name, packageNameProvider) {
    override var key: String? = null

    override var isRemote: Boolean = false

    override var isRemoteValueDefault: Boolean = true

    override var description: String? = null

    override var isPublic: Boolean = true

    /**
     * Adds an option and marks it as the default value.
     *
     * This must be called exactly once.
     */
    public fun withDefaultOption(name: String): Unit = withOption(name, isDefault = true)

    /** Adds a non-default option to the feature flag. */
    public fun withOption(name: String): Unit = withOption(name, isDefault = false)

    internal companion object {
      private const val serialVersionUID = 1L
    }
  }

  /** Binary feature flag with `Enabled` and `Disabled` states. */
  @LaboratoryDsl
  public class BinaryOption
  internal constructor(name: String, isEnabled: Boolean, packageNameProvider: PackageNameProvider) :
    FeatureFlagInput(name, packageNameProvider) {
    override var key: String? = null

    override var isRemote: Boolean = false

    override var isRemoteValueDefault: Boolean = true

    override var description: String? = null

    override var isPublic: Boolean = true

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
