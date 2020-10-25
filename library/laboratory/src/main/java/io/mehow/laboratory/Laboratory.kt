package io.mehow.laboratory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * High-level API for interaction with feature flags. It allows to read and write their values.
 *
 * @param storage [FeatureStorage] delegate that will persist all feature flags that go through this laboratory.
 */
class Laboratory(
  private val storage: FeatureStorage,
) {
  /**
   * Observes any changes to the input [Feature].
   */
  inline fun <reified T : Feature<T>> observe() = observe(T::class.java)

  /**
   * Observes any changes to the input [Feature].
   */
  fun <T : Feature<T>> observe(featureClass: Class<T>): Flow<T> {
    val (features, defaultFeature) = extractFeatureMetadata(featureClass)
    return storage.observeFeatureName(featureClass).map { featureName ->
      val expectedName = featureName ?: defaultFeature.name
      features.firstOrNull { it.name == expectedName } ?: defaultFeature
    }
  }

  /**
   * Returns the current value of the input [Feature].
   */
  suspend inline fun <reified T : Feature<T>> experiment() = experiment(T::class.java)

  /**
   * Returns the current value of the input [Feature]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  inline fun <reified T : Feature<T>> experimentBlocking() = runBlocking { experiment<T>() }

  /**
   * Returns the current value of the input [Feature].
   */
  suspend fun <T : Feature<T>> experiment(featureClass: Class<T>): T {
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
  fun <T : Feature<T>> experimentBlocking(featureClass: Class<T>) = runBlocking { experiment(featureClass) }

  /**
   * Checks if a [Feature] is set to the input [value].
   */
  suspend fun <T : Feature<T>> experimentIs(value: T) = experiment(value::class.java) == value

  /**
   * Checks if a [Feature] is set to the input [value]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  fun <T : Feature<T>> experimentIsBlocking(value: T) = runBlocking { experimentIs(value) }

  /**
   * Sets a [Feature] to have the input [value].
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  suspend fun <T : Feature<*>> setFeature(value: T) = storage.setFeature(value)

  /**
   * Sets a [Feature] to have the input [value]. Warning – this call can block the calling thread.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   * @see BlockingIoCall
   */
  @BlockingIoCall
  fun <T : Feature<*>> setFeatureBlocking(value: T) = runBlocking { setFeature(value) }

  /**
   * Sets [Features][Feature] to have the input [values]. If [values] contains more than one value
   * for the same feature flag, the last one should be applied.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  suspend fun <T : Feature<*>> setFeatures(vararg values: T) = storage.setFeatures(*values)

  /**
   * Sets [Features][Feature] to have the input [values]. If [values] contains more than one value
   * for the same feature flag, the last one should be applied. Warning – this call can block the calling thread.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   * @see BlockingIoCall
   */
  @BlockingIoCall
  fun <T : Feature<*>> setFeaturesBlocking(vararg values: T) = runBlocking { setFeatures(*values) }

  private fun <T : Feature<T>> extractFeatureMetadata(group: Class<T>): Pair<Array<T>, T> {
    val features = requireNotNull(group.enumConstants) { "${group.name} must be an enum" }
    require(features.isNotEmpty()) { "${group.name} must have at least one value" }
    val defaultFeature = features.firstOrNull { it.isDefaultValue } ?: features.first()
    return features to defaultFeature
  }

  companion object {
    /**
     * Creates [Laboratory] with an in-memory persistence mechanism.
     */
    fun inMemory() = Laboratory(FeatureStorage.inMemory())
  }
}
