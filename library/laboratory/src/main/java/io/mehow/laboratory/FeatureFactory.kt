package io.mehow.laboratory

/**
 * Factory that should provide all available feature flags. There shouldn't be any need to use it in a
 * regular application code. Its main purpose is for QA inspection module.
 */
interface FeatureFactory {
  /**
   * Returns set of all available feature flags.
   */
  fun create(): Set<Class<Feature<*>>>

  companion object
}
