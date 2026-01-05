package io.mehow.laboratory.inspector

/** Specifies how deprecated feature flags are positioned within a list of features. */
public enum class FeatureAlignment {
  /** No special ordering; deprecated feature flags remain in their normal positions in the list. */
  Regular,

  /** Moves all deprecated feature flags to the bottom of the list. */
  Bottom;

  /** Interface to choose a [FeatureAlignment] for a feature based on its [DeprecationLevel]. */
  public fun interface Selector {
    /** Returns the appropriate [FeatureAlignment] given a feature's deprecation level. */
    public fun select(level: DeprecationLevel): FeatureAlignment
  }
}
