package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
import org.gradle.api.Action
import java.io.Serializable

/**
 * An entry point for configuration of supervised feature flags code generation.
 */
public class ChildFeatureFlagsInput internal constructor(
  private val packageNameProvider: PackageNameProvider,
  private val supervisor: SupervisorInput,
) : Serializable {
  private val featureInputs = mutableListOf<FeatureFlagInput>()

  /**
   * Generates a new supervised [multi-option][FeatureFlagInput.MultiOption] feature flag.
   */
  public fun feature(
    name: String,
    action: Action<FeatureFlagInput.MultiOption>,
  ) {
    featureInputs += FeatureFlagInput.MultiOption(name, packageNameProvider, supervisor).apply(action::execute)
  }

  /**
   * Generates a new supervised [binary][FeatureFlagInput.BinaryOption] feature flag that is enabled by default.
   */
  @JvmOverloads
  public fun enabledFeature(
    name: String,
    action: Action<FeatureFlagInput.BinaryOption> = Action { },
  ) {
    binaryFeature(name, isEnabled = true, action)
  }

  /**
   * Generates a new supervised [binary][FeatureFlagInput.BinaryOption] feature flag that is disabled by default.
   */
  @JvmOverloads
  public fun disabledFeature(
    name: String,
    action: Action<FeatureFlagInput.BinaryOption> = Action { },
  ) {
    binaryFeature(name, isEnabled = false, action)
  }

  private fun binaryFeature(
    name: String,
    isEnabled: Boolean,
    action: Action<FeatureFlagInput.BinaryOption>,
  ) {
    featureInputs += FeatureFlagInput.BinaryOption(name, isEnabled, packageNameProvider, supervisor)
      .apply(action::execute)
  }

  internal fun toModels(): List<FeatureFlagModel> = featureInputs.flatMap(FeatureFlagInput::toModelsWithChildren)

  internal companion object {
    private const val serialVersionUID = 0L
  }
}
