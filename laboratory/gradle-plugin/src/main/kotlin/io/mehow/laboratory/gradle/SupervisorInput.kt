package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.Supervisor
import java.io.Serializable

internal class SupervisorInput(
  private val featureFlagInput: FeatureFlagInput,
  private val optionInput: FeatureFlagOptionInput,
) : Serializable {
  fun toModel(): Supervisor = Supervisor(featureFlagInput.toModel(), optionInput.toModel())

  internal companion object {
    private const val serialVersionUID = 0L
  }
}
