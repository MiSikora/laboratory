package io.mehow.laboratory.gradle

import org.gradle.api.Action

open class LaboratoryExtension {
  var packageName: String = ""

  internal var featureInputs: List<FeatureFlagInput> = emptyList()
    private set

  fun feature(name: String, action: Action<FeatureFlagInput>) {
    featureInputs += FeatureFlagInput(name).let { input ->
      action.execute(input)
      input.packageName = input.packageName ?: packageName
      return@let input
    }
  }

  internal var factoryInput: FeatureFactoryInput? = null
    private set

  fun featureFactory() = featureFactory { }

  fun featureFactory(action: Action<FeatureFactoryInput>) {
    factoryInput = FeatureFactoryInput().let { input ->
      action.execute(input)
      input.packageName = input.packageName ?: packageName
      return@let input
    }
  }

  internal var storageInput: SourcedFeatureStorageInput? = null
    private set

  fun sourcedStorage() = sourcedStorage { }

  fun sourcedStorage(action: Action<SourcedFeatureStorageInput>) {
    storageInput = SourcedFeatureStorageInput().let { input ->
      action.execute(input)
      input.packageName = input.packageName ?: packageName
      return@let input
    }
  }

  internal var featureSourcesFactory: FeatureFactoryInput? = null
    private set

  fun featureSourceFactory() = featureSourceFactory { }

  fun featureSourceFactory(action: Action<FeatureFactoryInput>) {
    featureSourcesFactory = FeatureFactoryInput().let { input ->
      action.execute(input)
      input.packageName = input.packageName ?: packageName
      return@let input
    }
  }
}
