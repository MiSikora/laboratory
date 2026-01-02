package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagOption
import java.io.Serializable

internal class FeatureFlagOptionInput(private val name: String, private val isDefault: Boolean) :
  Serializable {
  fun toModel() = FeatureFlagOption(name, isDefault)

  internal companion object {
    private const val serialVersionUID = 1L
  }
}
