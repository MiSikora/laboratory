package io.mehow.laboratory

/**
 * Factory that allows to override default feature flag option used by [Laboratory].
 *
 * @see [Feature.defaultOption]
 */
public interface DefaultOptionFactory {
  /**
   * Returns override for a default option or `null` if there should be no override.
   *
   * **Warning** – returned default option must be of the same type as [feature]. Otherwise it will throw an exception.
   */
  public fun <T : Feature<T>> create(feature: T): Feature<*>?

  /**
   * Creates a new [DefaultOptionFactory] that will first look for a default value in this factory and then in the
   * other factory.
   */
  @JvmDefault
  public operator fun plus(factory: DefaultOptionFactory): DefaultOptionFactory = object : DefaultOptionFactory {
    override fun <T : Feature<T>> create(
      feature: T,
    ) = this@DefaultOptionFactory.create(feature) ?: factory.create(feature)
  }
}
