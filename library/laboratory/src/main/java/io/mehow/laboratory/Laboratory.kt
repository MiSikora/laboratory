package io.mehow.laboratory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * High-level API for interaction with feature flags. It allows to read and write their values.
 *
 * @param storage [FeatureStorage] delegate that will persist all feature flags that go through this laboratory.
 */
public class Laboratory(
  private val storage: FeatureStorage,
) {
  /**
   * Observes any changes to the input [Feature].
   */
  public inline fun <reified T : Feature<T>> observe(): Flow<T> = observe(T::class.java)

  /**
   * Observes any changes to the input [Feature].
   */
  public fun <T : Feature<T>> observe(feature: Class<T>): Flow<T> {
    val options = feature.options
    val defaultOption = feature.defaultOption
    return storage.observeFeatureName(feature).map { featureName ->
      val expectedName = featureName ?: defaultOption.name
      options.firstOrNull { it.name == expectedName } ?: defaultOption
    }
  }

  /**
   * Returns the current value of the input [Feature].
   */
  public suspend inline fun <reified T : Feature<T>> experiment(): T = experiment(T::class.java)

  /**
   * Returns the current value of the input [Feature]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public inline fun <reified T : Feature<T>> experimentBlocking(): T = runBlocking { experiment<T>() }

  /**
   * Returns the current value of the input [Feature].
   */
  public suspend fun <T : Feature<T>> experiment(feature: Class<T>): T {
    val options = feature.options
    val defaultOption = feature.defaultOption
    val expectedName = storage.getFeatureName(defaultOption.javaClass) ?: defaultOption.name
    return options.firstOrNull { it.name == expectedName } ?: defaultOption
  }

  /**
   * Returns the current value of the input [Feature]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<T>> experimentBlocking(feature: Class<T>): T = runBlocking { experiment(feature) }

  /**
   * Checks if a [Feature] is set to the input [option].
   */
  public suspend fun <T : Feature<T>> experimentIs(option: T): Boolean = experiment(option::class.java) == option

  /**
   * Checks if a [Feature] is set to the input [option]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<T>> experimentIsBlocking(option: T): Boolean = runBlocking { experimentIs(option) }

  /**
   * Sets a [Feature] to have the input [option].
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  public suspend fun <T : Feature<*>> setOption(option: T): Boolean = storage.setOption(option)

  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'setOption()' instead.",
      replaceWith = ReplaceWith("setOption(option)"),
  )
  public suspend fun <T : Feature<*>> setFeature(option: T): Boolean = setOption(option)

  /**
   * Sets a [Feature] to have the input [option]. Warning – this call can block the calling thread.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<*>> setOptionBlocking(option: T): Boolean = runBlocking { setOption(option) }

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'setOptionBlocking()' instead.",
      replaceWith = ReplaceWith("setOptionBlocking(option)"),
  )
  public fun <T : Feature<*>> setFeatureBlocking(option: T): Boolean = setOptionBlocking(option)

  /**
   * Sets [Features][Feature] to have the input [options]. If [options] contains more than one value
   * for the same feature flag, the last one should be applied.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  public suspend fun <T : Feature<*>> setOptions(vararg options: T): Boolean = storage.setOptions(*options)

  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'setOptions()' instead.",
      replaceWith = ReplaceWith("setOptions(*options)"),
  )
  public suspend fun <T : Feature<*>> setFeatures(vararg options: T): Boolean = setOptions(*options)

  /**
   * Sets [Features][Feature] to have the input [options]. If [options] contains more than one value
   * for the same feature flag, the last one should be applied. Warning – this call can block the calling thread.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<*>> setOptionsBlocking(vararg options: T): Boolean = runBlocking { setOptions(*options) }

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'setOptionsBlocking()' instead.",
      replaceWith = ReplaceWith("setOptionsBlocking(*options)"),
  )
  public fun <T : Feature<*>> setFeaturesBlocking(vararg options: T): Boolean = setOptionsBlocking(*options)

  public companion object {
    /**
     * Creates [Laboratory] with an in-memory persistence mechanism.
     */
    public fun inMemory(): Laboratory = Laboratory(FeatureStorage.inMemory())
  }
}
