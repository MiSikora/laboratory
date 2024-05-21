package io.mehow.laboratory.gradle

/**
 * Possible contributions of a dependency to feature flag generation. See [LaboratoryExtension.dependency]
 * for more info.
 */
public enum class DependencyContribution {
  /** Contribute to [LaboratoryExtension.featureFactory]. */
  FeatureFactory,

  /** Contribute to [LaboratoryExtension.featureSourceFactory]. */
  FeatureSourceFactory,

  /** Contribute to [LaboratoryExtension.optionFactory]. */
  OptionFactory,

  /** Contribute to [LaboratoryExtension.sourcedStorage]. */
  SourcedStorage,
}
