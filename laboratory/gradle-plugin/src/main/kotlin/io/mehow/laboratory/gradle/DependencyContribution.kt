package io.mehow.laboratory.gradle

/** Specifies which generated outputs should include feature flags from a dependent project. */
public enum class DependencyContribution {
  /** Include dependent features in the generated feature factory. */
  FeatureFactory,
  /** Include dependent features in the generated option factory. */
  OptionFactory,
}
