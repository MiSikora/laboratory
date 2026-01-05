package io.mehow.laboratory.inspector

/** Specifies how deprecated feature flags are displayed in the UI. */
public enum class FeatureStyle {
  /**
   * Show deprecated feature flags with no special styling (they appear the same as non-deprecated
   * ones).
   */
  Show,

  /** Display the feature flag's name with a strikethrough to indicate it is deprecated. */
  Strikethrough,

  /** Remove the feature flag from the list (it will not be shown in the UI). */
  Hide;

  /** Interface to select a [FeatureStyle] based on a feature's [DeprecationLevel]. */
  public fun interface Selector {
    /** Returns the appropriate [FeatureStyle] for the given feature's deprecation level. */
    public fun select(level: DeprecationLevel): FeatureStyle
  }
}
