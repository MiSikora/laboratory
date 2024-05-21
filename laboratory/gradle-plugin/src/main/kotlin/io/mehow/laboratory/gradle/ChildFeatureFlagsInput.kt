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
  private val mutableFeatureInputs = mutableListOf<FeatureFlagInput>()

  private val featureInputs: List<FeatureFlagInput> = mutableFeatureInputs

  /**
   * Generates a new supervised feature flag.
   */
  public fun feature(
    name: String,
    action: Action<FeatureFlagInput>,
  ) {
    mutableFeatureInputs += FeatureFlagInput(name, packageNameProvider, supervisor).let { input ->
      action.execute(input)
      return@let input
    }
  }

  internal fun toModels(): List<FeatureFlagModel> = featureInputs.flatMap(FeatureFlagInput::toModels)

  internal companion object {
    private const val serialVersionUID = 0L
  }
}
