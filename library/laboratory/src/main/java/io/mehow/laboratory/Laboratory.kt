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
  public fun <T : Feature<T>> observe(featureClass: Class<T>): Flow<T> {
    val (features, defaultFeature) = extractFeatureMetadata(featureClass)
    return storage.observeFeatureName(featureClass).map { featureName ->
      val expectedName = featureName ?: defaultFeature.name
      features.firstOrNull { it.name == expectedName } ?: defaultFeature
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
  public suspend fun <T : Feature<T>> experiment(featureClass: Class<T>): T {
    val (features, defaultFeature) = extractFeatureMetadata(featureClass)
    val expectedName = storage.getFeatureName(defaultFeature.javaClass) ?: defaultFeature.name
    return features.firstOrNull { it.name == expectedName } ?: defaultFeature
  }

  /**
   * Returns the current value of the input [Feature]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<T>> experimentBlocking(featureClass: Class<T>): T = runBlocking { experiment(featureClass) }

  /**
   * Checks if a [Feature] is set to the input [value].
   */
  public suspend fun <T : Feature<T>> experimentIs(value: T): Boolean = experiment(value::class.java) == value

  /**
   * Checks if a [Feature] is set to the input [value]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<T>> experimentIsBlocking(value: T): Boolean = runBlocking { experimentIs(value) }

  /**
   * Sets a [Feature] to have the input [value].
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  public suspend fun <T : Feature<*>> setFeature(value: T): Boolean = storage.setFeature(value)

  /**
   * Sets a [Feature] to have the input [value]. Warning – this call can block the calling thread.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<*>> setFeatureBlocking(value: T): Boolean = runBlocking { setFeature(value) }

  /**
   * Sets [Features][Feature] to have the input [values]. If [values] contains more than one value
   * for the same feature flag, the last one should be applied.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  public suspend fun <T : Feature<*>> setFeatures(vararg values: T): Boolean = storage.setFeatures(*values)

  /**
   * Sets [Features][Feature] to have the input [values]. If [values] contains more than one value
   * for the same feature flag, the last one should be applied. Warning – this call can block the calling thread.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<*>> setFeaturesBlocking(vararg values: T): Boolean = runBlocking { setFeatures(*values) }

  private fun <T : Feature<T>> extractFeatureMetadata(group: Class<T>): Pair<Array<T>, T> {
    val features = requireNotNull(group.enumConstants) { "${group.name} must be an enum" }
    require(features.isNotEmpty()) { "${group.name} must have at least one value" }
    val defaultFeature = features.firstOrNull { it.isDefaultValue } ?: features.first()
    return features to defaultFeature
  }

  public companion object {
    /**
     * Creates [Laboratory] with an in-memory persistence mechanism.
     */
    public fun inMemory(): Laboratory = Laboratory(FeatureStorage.inMemory())
  }
}
