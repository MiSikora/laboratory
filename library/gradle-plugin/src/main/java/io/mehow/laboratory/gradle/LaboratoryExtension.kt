package io.mehow.laboratory.gradle

import org.gradle.api.Action

open class LaboratoryExtension {
  var packageName: String = ""

  internal var factoryInput: FeatureFactoryInput? = null
    private set

  fun factory() = factory(Action<FeatureFactoryInput> { })

  fun factory(action: Action<FeatureFactoryInput>) {
    factoryInput = FeatureFactoryInput().let { input ->
      action.execute(input)
      input.packageName = input.packageName ?: packageName
      return@let input
    }
  }

  internal var featureInputs: List<FeatureFlagInput> = emptyList()
    private set

  fun feature(name: String, action: Action<FeatureFlagInput>) {
    featureInputs += FeatureFlagInput(name).let { input ->
      action.execute(input)
      input.packageName = input.packageName ?: packageName
      return@let input
    }
  }
}
