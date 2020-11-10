package io.mehow.laboratory

import kotlinx.coroutines.flow.Flow

/**
 * Persistence mechanism for feature flags.
 */
public interface FeatureStorage {
  /**
   * Observes changes to currently selected feature flag name.
   * If feature flag is not available, it should emit `null`.
   */
  public fun <T : Feature<*>> observeFeatureName(featureClass: Class<T>): Flow<String?>

  /**
   * Returns the current value of a selected feature flag name.
   * If feature flag is not available, it should return `null`.
   */
  public suspend fun <T : Feature<*>> getFeatureName(featureClass: Class<T>): String?

  /**
   * Sets [Features][Feature] to have the input [values]. If [values] contains more than one value
   * for the same feature flag, the last one should be applied.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  public suspend fun <T : Feature<*>> setFeatures(vararg values: T): Boolean

  /**
   * Sets a [Feature] to have the input [value].
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  @JvmDefault public suspend fun <T : Feature<*>> setFeature(value: T): Boolean = setFeatures(value)

  public companion object {
    /**
     * Creates [FeatureStorage] that saves feature flags in app's memory.
     */
    public fun inMemory(): FeatureStorage = InMemoryFeatureStorage()

    /**
     * Creates [FeatureStorage] that is aware of different sources for feature flag values.
     * For example, the following code will be able to produce values for local, Firebase and Aws sources,
     * and will automatically switch reads based on currently selected sources for feature flags.
     *
     * ```
     * FeatureStorage.sourced(
     *   localSource = FeatureStorage.inMemory(),
     *   remoteSources = mapOf(
     *     "Firebase" to FeatureStorage.inMemory(),
     *     "Aws" to FeatureStorage.inMemory(),
     *   ),
     * )
     * ```
     *
     * In order to connect remote sources with sources of feature flag sources they need to match their names.
     * If you use Gradle plugin, you should not use this method as a more specialised factory method
     * will be generated for you, that will make sure that all remote sources are configured.
     *
     * @see [Feature.source]
     */
    public fun sourced(
      localSource: FeatureStorage,
      remoteSources: Map<String, FeatureStorage>,
    ): FeatureStorage = SourcedFeatureStorage(localSource, remoteSources)
  }
}
