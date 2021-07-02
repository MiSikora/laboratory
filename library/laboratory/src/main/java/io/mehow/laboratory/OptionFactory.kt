package io.mehow.laboratory

/**
 * Factory that returns a matching option. Matching rules are up to the implementation details.
 */
public interface OptionFactory {
  /**
   * Returns a feature matching class name and option name or null if no match is found.
   */
  public fun create(key: String, name: String): Feature<*>?

  /**
   * Creates a new [OptionFactory] that will first look for an option in this factory and then in the
   * other factory.
   */
  public operator fun plus(factory: OptionFactory): OptionFactory = object : OptionFactory {
    override fun create(key: String, name: String) =
      this@OptionFactory.create(key, name) ?: factory.create(key, name)
  }

  public companion object
}
