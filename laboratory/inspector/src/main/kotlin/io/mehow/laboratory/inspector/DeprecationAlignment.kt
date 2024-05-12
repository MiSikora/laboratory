package io.mehow.laboratory.inspector

/**
 * Alignment of deprecated feature flags in a list.
 */
public enum class DeprecationAlignment {
  /**
   * Does not place feature flags in any particular place of a list.
   */
  Regular,

  /**
   * Places deprecated feature flags at the bottom of a list.
   */
  Bottom,
  ;

  /**
   * Determines alignment of features flags in a list based on their [DeprecationLevel].
   */
  public fun interface Selector {
    /**
     * Selects [DeprecationAlignment] based on a feature flag deprecation level.
     */
    public fun select(level: DeprecationLevel): DeprecationAlignment
  }
}
