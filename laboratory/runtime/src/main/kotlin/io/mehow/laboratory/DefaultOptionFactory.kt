package io.mehow.laboratory

/**
 * A factory for providing alternate default options for features.
 *
 * Allows dynamic override of the default option used by a feature if no other value is selected.
 * This can be useful in different runtime environments where default behavior needs to differ.
 *
 * If no override is provided for a feature, its declared default option will be used.
 */
public interface DefaultOptionFactory {
  /**
   * Returns a default option for the provided feature, or `null` if the feature should not be
   * overridden.
   *
   * Warning: The returned option must match the runtime type of the feature. Returning an option of
   * an incorrect type will cause a runtime exception.
   */
  public fun <T : Feature<out T>> create(feature: T): Feature<*>?

  /**
   * Combines this factory with another. The resulting factory first checks this factory, and then
   * the provided one if no override is found.
   */
  public operator fun plus(factory: DefaultOptionFactory): DefaultOptionFactory =
    object : DefaultOptionFactory {
      override fun <T : Feature<out T>> create(feature: T) =
        this@DefaultOptionFactory.create(feature) ?: factory.create(feature)
    }

  public companion object
}

internal class SafeDefaultOptionFactory(private val delegate: DefaultOptionFactory?) {
  fun <T : Feature<out T>> create(feature: Class<out T>): T {
    val defaultOption = delegate?.create(feature.firstOption) ?: return feature.defaultOption
    check(defaultOption::class.java == feature) {
      val optionName = "${defaultOption::class.java.simpleName}.$defaultOption"
      val featureName = feature.canonicalName
      "Tried to use $optionName as a default option for $featureName"
    }
    @Suppress("UNCHECKED_CAST")
    return defaultOption as T
  }
}

private val <T : Feature<out T>> Class<out T>.defaultOption: T
  get() = firstOption.defaultOption
