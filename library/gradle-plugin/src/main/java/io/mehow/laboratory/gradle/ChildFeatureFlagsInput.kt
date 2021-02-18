package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.Supervisor.Builder
import org.gradle.api.Action

/**
 * An entry point for configuration of supervised feature flags code generation.
 */
public class ChildFeatureFlagsInput internal constructor(
  private val packageNameProvider: () -> String,
  private val supervisor: () -> Builder,
) {
  private val mutableFeatureInputs = mutableListOf<FeatureFlagInput>()

  private val featureInputs: List<FeatureFlagInput> = mutableFeatureInputs

  /**
   * Generates a new supervised feature flag.
   */
  public fun feature(name: String, action: Action<FeatureFlagInput>) {
    mutableFeatureInputs += FeatureFlagInput(name, packageNameProvider, supervisor).let { input ->
      action.execute(input)
      return@let input
    }
  }

  internal fun toBuilders() = featureInputs.flatMap(FeatureFlagInput::toBuilders)
}
