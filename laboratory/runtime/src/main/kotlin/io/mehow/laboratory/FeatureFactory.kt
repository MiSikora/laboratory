package io.mehow.laboratory

/**
 * Factory that provides a set of known feature flags.
 *
 * Mainly used in QA tooling and debug inspection modules to expose available features. This should
 * not be needed in normal application logic.
 */
public interface FeatureFactory {
  /** Returns all known feature flag types. */
  public fun create(): Set<Class<out Feature<*>>>

  /** Combines this factory with another. The resulting factory returns the union of both sets. */
  public operator fun plus(factory: FeatureFactory): FeatureFactory =
    object : FeatureFactory {
      override fun create() = this@FeatureFactory.create() + factory.create()
    }

  public companion object
}
