package io.mehow.laboratory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * High-level API for interaction with feature flags. It allows to read and write their options.
 */
public class Laboratory internal constructor(
  builder: Builder,
) {
  private val storage = builder.storage.let { storage ->
    val optionFactory = builder.defaultOptionFactory
    if (optionFactory != null && storage is SourcedFeatureStorage) {
      storage.withDefaultOptionFactory(optionFactory)
    } else {
      storage
    }
  }
  private val defaultOptionFactory = builder.defaultOptionFactory?.let(::SafeDefaultOptionFactory)
  private val blockingLaboratory = BlockingLaboratory(this)

  /**
   * An entry point for blocking API.
   *
   * @see BlockingIoCall
   */
  public fun blocking(): BlockingLaboratory = blockingLaboratory

  /**
   * Observes any changes to the input [Feature].
   */
  public inline fun <reified T : Feature<out T>> observe(): Flow<T> = observe(T::class.java)

  /**
   * Observes any changes to the input [Feature].
   */
  @Suppress("UNCHECKED_CAST")
  public fun <T : Feature<out T>> observe(
    feature: Class<out T>,
  ): Flow<T> = observeRaw(feature as Class<Feature<*>>) as Flow<T>

  private fun observeRaw(feature: Class<Feature<*>>): Flow<Feature<*>> {
    val options = feature.options
    val defaultOption = getDefaultOption(feature)

    val activeOption = storage.observeFeatureName(feature).map { parentName ->
      val expectedName = parentName ?: defaultOption.name
      options.firstOrNull { it.name == expectedName } ?: defaultOption
    }

    val supervisor = feature.supervisorOption ?: return activeOption
    return combine(activeOption, observeRaw(supervisor.javaClass)) { option, parentOption ->
      if (option.supervisorOption != parentOption) defaultOption else option
    }.distinctUntilChanged()
  }

  /**
   * Returns the current option of the input [Feature].
   */
  public suspend inline fun <reified T : Feature<out T>> experiment(): T = experiment(T::class.java)

  /**
   * Returns the current option of the input [Feature].
   */
  @Suppress("UNCHECKED_CAST")
  public suspend fun <T : Feature<out T>> experiment(
    feature: Class<out T>,
  ): T = experimentRaw(feature as Class<Feature<*>>) as T

  private suspend fun experimentRaw(feature: Class<Feature<*>>): Feature<*> {
    val options = feature.options
    val defaultOption = getDefaultOption(feature)
    val expectedName = storage.getFeatureName(defaultOption.javaClass) ?: defaultOption.name
    val activeOption = options.firstOrNull { it.name == expectedName } ?: defaultOption

    val parent = feature.supervisorOption ?: return activeOption
    return if (activeOption.supervisorOption != experimentRaw(parent.javaClass)) defaultOption else activeOption
  }

  /**
   * Checks if a [Feature] is set to the input [option].
   */
  @Suppress("UNCHECKED_CAST")
  public suspend fun <T : Feature<out T>> experimentIs(option: T): Boolean = experimentRaw(
      option::class.java as Class<Feature<*>>
  ) == option

  /**
   * Sets a [Feature] to have the input [option].
   *
   * @return `true` if the option was set successfully, `false` otherwise.
   */
  public suspend fun <T : Feature<*>> setOption(option: T): Boolean = storage.setOption(option)

  /**
   * Sets [Features][Feature] to have the input [options]. If [options] contains more than one option
   * for the same feature flag, the last one should be applied.
   *
   * @return `true` if the option was set successfully, `false` otherwise.
   */
  public suspend fun <T : Feature<*>> setOptions(vararg options: T): Boolean =
    storage.setOptions(*options)

  /**
   * Removes all stored feature flag options.
   *
   * @return `true` if the option was set successfully, `false` otherwise.
   */
  public suspend fun clear(): Boolean = storage.clear()

  private fun <T : Feature<out T>> getDefaultOption(
    feature: Class<out T>,
  ) = defaultOptionFactory?.create(feature) ?: feature.defaultOption

  public companion object {
    /**
     * Creates [Laboratory] with an in-memory persistence mechanism.
     */
    public fun inMemory(): Laboratory = create(FeatureStorage.inMemory())

    /**
     * Creates [Laboratory] with a provided [storage].
     *
     * @param storage [FeatureStorage] delegate that will persist all feature flags.
     */
    public fun create(storage: FeatureStorage): Laboratory =
      builder().featureStorage(storage).build()

    /**
     * Creates a builder that allows to customize [Laboratory].
     */
    public fun builder(): FeatureStorageStep = Builder()
  }

  internal class Builder : FeatureStorageStep, BuildingStep {
    lateinit var storage: FeatureStorage

    override fun featureStorage(storage: FeatureStorage): BuildingStep = apply {
      this.storage = storage
    }

    var defaultOptionFactory: DefaultOptionFactory? = null

    override fun defaultOptionFactory(factory: DefaultOptionFactory): BuildingStep = apply {
      this.defaultOptionFactory = factory
    }

    override fun build(): Laboratory = Laboratory(this)
  }

  /**
   * A step of a fluent builder that requires [FeatureStorage] to proceed.
   */
  public interface FeatureStorageStep {
    /**
     * Sets a feature storage that will be used by [Laboratory].
     */
    public fun featureStorage(storage: FeatureStorage): BuildingStep
  }

  /**
   * The final step of a fluent builder that can set optional parameters.
   */
  public interface BuildingStep {
    /**
     * Sets a factory that can provide default options override.
     */
    public fun defaultOptionFactory(factory: DefaultOptionFactory): BuildingStep

    /**
     * Creates a new [Laboratory] with provided parameters.
     */
    public fun build(): Laboratory
  }
}
