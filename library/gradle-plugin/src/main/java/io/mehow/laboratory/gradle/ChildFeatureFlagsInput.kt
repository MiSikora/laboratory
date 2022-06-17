package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.Supervisor
import org.gradle.api.Action

/**
 * An entry point for configuration of supervised feature flags code generation.
 */
public class ChildFeatureFlagsInput internal constructor(
  private val parentPackageName: String,
  private val supervisor: () -> Supervisor,
) {
  private val mutableFeatureInputs = mutableListOf<FeatureFlagInput>()

  private val featureInputs: List<FeatureFlagInput> = mutableFeatureInputs

  /**
   * Generates a new supervised feature flag.
   */
  public fun feature(name: String, action: Action<FeatureFlagInput>) {
    mutableFeatureInputs += FeatureFlagInput(name, parentPackageName, supervisor).let { input ->
      action.execute(input)
      return@let input
    }
  }

  internal fun toModels() = featureInputs.flatMap(FeatureFlagInput::toModels)
}
