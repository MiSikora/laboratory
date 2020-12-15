package io.mehow.laboratory

/**
 * Factory that should provide all available feature flags. There shouldn't be any need to use it in a
 * regular application code. Its main purpose is for QA inspection module.
 */
public interface FeatureFactory {
  /**
   * Returns set of all available feature flags.
   */
  public fun create(): Set<Class<Feature<*>>>

  /**
   * Creates a new [FeatureFactory] that will return a combined set of this factory and the other factory.
   */
  @JvmDefault
  public operator fun plus(factory: FeatureFactory): FeatureFactory = object : FeatureFactory {
    override fun create() = this@FeatureFactory.create() + factory.create()
  }

  public companion object
}
