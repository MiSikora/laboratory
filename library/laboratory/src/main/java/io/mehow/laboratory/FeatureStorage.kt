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
  public fun observeFeatureName(feature: Class<out Feature<*>>): Flow<String?>

  /**
   * Returns the current value of a selected feature flag name.
   * If feature flag is not available, it should return `null`.
   */
  public suspend fun getFeatureName(feature: Class<out Feature<*>>): String?

  /**
   * Sets [Features][Feature] to have the input [options]. If [options] contains more than one value
   * for the same feature flag, the last one should be applied.
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  public suspend fun setOptions(vararg options: Feature<*>): Boolean

  /**
   * Removes all stored feature flag options.
   */
  public suspend fun clear(): Boolean

  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'setOptions()' instead.",
      replaceWith = ReplaceWith("setOptions(*options)"),
  )
  public suspend fun setFeatures(vararg options: Feature<*>): Boolean = setOptions(*options)

  /**
   * Sets a [Feature] to have the input [option].
   *
   * @return `true` if the value was set successfully, `false` otherwise.
   */
  public suspend fun setOption(option: Feature<*>): Boolean = setOptions(option)

  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'setOption()' instead.",
      replaceWith = ReplaceWith("setOption(option)"),
  )
  public suspend fun setFeature(option: Feature<*>): Boolean = setOption(option)

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
