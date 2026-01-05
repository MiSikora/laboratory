package io.mehow.laboratory

/**
 * A factory for providing alternate default sources for features.
 *
 * Can be used to dynamically control whether a feature should default to local or remote
 * evaluation. This can be useful in different runtime environments where default behavior needs to
 * differ.
 *
 * If no override is provided, the feature's declared default source will be used.
 */
public interface DefaultSourceFactory {
  /**
   * Combines this factory with another. The resulting factory first checks this factory, and then
   * the provided one if no override is found.
   */
  public fun <T : Feature<out T>> create(feature: T): Feature.Source?

  public operator fun plus(factory: DefaultSourceFactory): DefaultSourceFactory =
    object : DefaultSourceFactory {
      override fun <T : Feature<out T>> create(feature: T) =
        this@DefaultSourceFactory.create(feature) ?: factory.create(feature)
    }

  public companion object
}

internal class SafeDefaultSourceFactory(private val delegate: DefaultSourceFactory?) {
  fun <T : Feature<out T>> create(feature: T): Feature.Source {
    return delegate?.create(feature) ?: feature.defaultSource
  }
}
