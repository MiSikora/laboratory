package io.mehow.laboratory

/**
 * Factory that returns a matching feature option based on class and option name.
 *
 * Used during deserialization or lookup when resolving features from string identifiers.
 */
public interface OptionFactory {
  /**
   * Returns the feature option associated with the provided key and option name, or `null` if none
   * match.
   */
  public fun create(key: String, name: String): Feature<*>?

  /**
   * Combines this factory with another. The resulting factory first checks this factory, and then
   * the provided one if no match is found.
   */
  public operator fun plus(factory: OptionFactory): OptionFactory =
    object : OptionFactory {
      override fun create(key: String, name: String) =
        this@OptionFactory.create(key, name) ?: factory.create(key, name)
    }

  public companion object
}
