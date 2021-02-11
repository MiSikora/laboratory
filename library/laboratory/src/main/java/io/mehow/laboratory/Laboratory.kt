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
  private val storage = builder.storage
  private val defaultOptionFactory = builder.defaultOptionFactory?.let(::SafeDefaultOptionFactory)
  private val blockingLaboratory = BlockingLaboratory(this)

  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'Laboratory.create()' instead.",
      replaceWith = ReplaceWith("Laboratory.create(storage)"),
  )
  public constructor(storage: FeatureStorage) : this(Builder().apply { this.storage = storage })

  /**
   * An entry point for blocking API.
   *
   * @see BlockingIoCall
   */
  public fun blocking(): BlockingLaboratory = blockingLaboratory

  /**
   * Observes any changes to the input [Feature].
   */
  public inline fun <reified T : Feature<T>> observe(): Flow<T> = observe(T::class.java)

  /**
   * Observes any changes to the input [Feature].
   */
  public fun <T : Feature<T>> observe(feature: Class<T>): Flow<T> {
    val options = feature.options
    val defaultOption = getDefaultOption(feature)

    val activeOption = storage.observeFeatureName(feature).map { featureName ->
      val expectedName = featureName ?: defaultOption.name
      options.firstOrNull { it.name == expectedName } ?: defaultOption
    }

    val supervisor = feature.supervisorOption ?: return activeOption
    val activeParentOption = observeRaw(supervisor.javaClass)

    return combine(activeOption, activeParentOption) { option, parentOption ->
      if (option.supervisorOption != parentOption) defaultOption else option
    }.distinctUntilChanged()
  }

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
  public suspend inline fun <reified T : Feature<T>> experiment(): T = experiment(T::class.java)

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'blocking().experiment()' instead.",
      replaceWith = ReplaceWith("blocking().experiment()"),
  )
  public inline fun <reified T : Feature<T>> experimentBlocking(): T = blocking().experiment()

  /**
   * Returns the current option of the input [Feature].
   */
  public suspend fun <T : Feature<T>> experiment(feature: Class<T>): T {
    val options = feature.options
    val defaultOption = getDefaultOption(feature)
    val expectedName = storage.getFeatureName(defaultOption.javaClass) ?: defaultOption.name
    val activeOption = options.firstOrNull { it.name == expectedName } ?: defaultOption

    val parent = feature.supervisorOption ?: return activeOption
    return if (activeOption.supervisorOption != experimentRaw(parent.javaClass)) defaultOption else activeOption
  }

  private suspend fun experimentRaw(feature: Class<Feature<*>>): Feature<*> {
    val options = feature.options
    val defaultOption = getDefaultOption(feature)
    val expectedName = storage.getFeatureName(defaultOption.javaClass) ?: defaultOption.name
    val activeOption = options.firstOrNull { it.name == expectedName } ?: defaultOption

    val parent = feature.supervisorOption ?: return activeOption
    return if (activeOption.supervisorOption != experimentRaw(parent.javaClass)) defaultOption else activeOption
  }

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'blocking().experiment()' instead.",
      replaceWith = ReplaceWith("blocking().experiment(feature)"),
  )
  public fun <T : Feature<T>> experimentBlocking(feature: Class<T>): T = blocking().experiment(feature)

  /**
   * Checks if a [Feature] is set to the input [option].
   */
  public suspend fun <T : Feature<T>> experimentIs(option: T): Boolean = experiment(option::class.java) == option

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'blocking().experimentIs()' instead.",
      replaceWith = ReplaceWith("blocking().experimentIs(option)"),
  )
  public fun <T : Feature<T>> experimentIsBlocking(option: T): Boolean = blocking().experimentIs(option)

  /**
   * Sets a [Feature] to have the input [option].
   *
   * @return `true` if the option was set successfully, `false` otherwise.
   */
  public suspend fun <T : Feature<*>> setOption(option: T): Boolean = storage.setOption(option)

  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'setOption()' instead.",
      replaceWith = ReplaceWith("setOption(option)"),
  )
  public suspend fun <T : Feature<*>> setFeature(option: T): Boolean = setOption(option)

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'blocking().setOption(option)' instead.",
      replaceWith = ReplaceWith("blocking().setOption(option)"),
  )
  public fun <T : Feature<*>> setOptionBlocking(option: T): Boolean = blocking().setOption(option)

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'blocking().setOption()' instead.",
      replaceWith = ReplaceWith("blocking().setOption(option)"),
  )
  public fun <T : Feature<*>> setFeatureBlocking(option: T): Boolean = blocking().setOption(option)

  /**
   * Sets [Features][Feature] to have the input [options]. If [options] contains more than one option
   * for the same feature flag, the last one should be applied.
   *
   * @return `true` if the option was set successfully, `false` otherwise.
   */
  public suspend fun <T : Feature<*>> setOptions(vararg options: T): Boolean = storage.setOptions(*options)

  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'setOptions()' instead.",
      replaceWith = ReplaceWith("setOptions(*options)"),
  )
  public suspend fun <T : Feature<*>> setFeatures(vararg options: T): Boolean = setOptions(*options)

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'blocking().setOptions()' instead.",
      replaceWith = ReplaceWith("blocking().setOptions(*options)"),
  )
  public fun <T : Feature<*>> setOptionsBlocking(vararg options: T): Boolean = blocking().setOptions(*options)

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'blocking().setOptions()' instead.",
      replaceWith = ReplaceWith("blocking().setOptions(*options)"),
  )
  public fun <T : Feature<*>> setFeaturesBlocking(vararg options: T): Boolean = blocking().setOptions(*options)

  /**
   * Removes all stored feature flag options.
   *
   * @return `true` if the option was set successfully, `false` otherwise.
   */
  public suspend fun clear(): Boolean = storage.clear()

  @BlockingIoCall
  @Deprecated(
      message = "This method will be removed in 1.0.0. Use 'blocking().clear()' instead.",
      replaceWith = ReplaceWith("blocking().clear()"),
  )
  public fun clearBlocking(): Boolean = blocking().clear()

  private fun <T : Feature<T>> getDefaultOption(
    feature: Class<T>,
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
    public fun create(storage: FeatureStorage): Laboratory = builder().featureStorage(storage).build()

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
