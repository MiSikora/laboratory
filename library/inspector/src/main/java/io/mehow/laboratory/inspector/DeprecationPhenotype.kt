package io.mehow.laboratory.inspector

/**
 * UI representation of deprecated feature flags.
 */
public enum class DeprecationPhenotype {
  /**
   * Does not differentiate deprecated feature flags from not deprecated ones.
   */
  Show,

  /**
   * Strikes feature flag name through.
   */
  Strikethrough,

  /**
   * Removed feature flag from a list.
   */
  Hide,
  ;

  /**
   * Determines UI representation of feature flags based on their [DeprecationLevel].
   */
  public fun interface Selector {
    /**
     * Selects [DeprecationPhenotype] based on a feature flag deprecation level.
     */
    public fun select(level: DeprecationLevel): DeprecationPhenotype
  }
}
