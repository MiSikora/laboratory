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
}
