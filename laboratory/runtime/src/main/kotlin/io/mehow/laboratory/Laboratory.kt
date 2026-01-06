package io.mehow.laboratory

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * High-level API for interacting with feature flags using a two-source model (local and optional
 * remote). It provides suspend functions to read and write feature flag options in a non-blocking
 * way. If a remote source is configured, features that declare a remote source can retrieve options
 * from either local or remote storage based on the current source selection. This class should be
 * the main entry point for getting and setting feature flag values.
 */
public class Laboratory internal constructor(builder: Builder) {
  private val localStorage = SourceOptionStorage(builder.localStorage)
  private val remoteStorage = builder.remoteStorage?.let(::OptionStorage)
  private val sourceFactory = SafeDefaultSourceFactory(builder.defaultSourceFactory)
  private val optionFactory = SafeDefaultOptionFactory(builder.defaultOptionFactory)
  private val blockingLaboratory = BlockingLaboratory(this)

  public fun localStorage(): SourceOptionStorage = localStorage

  public fun remoteStorage(): OptionStorage? = remoteStorage

  /**
   * Returns a [BlockingLaboratory] that provides blocking (synchronous) versions of this
   * Laboratory's API.
   */
  public fun blocking(): BlockingLaboratory = blockingLaboratory

  /**
   * Observes changes to the specified [Feature] type. This returns a cold [Flow] that will emit the
   * current option of the feature and continue to emit new options whenever the feature's value
   * changes. The emission source (local or remote storage) is determined by the feature's
   * configured source: if a remote source is set for the feature and a remote storage is configured
   * in this Laboratory, the flow will reflect changes from the appropriate storage.
   */
  public inline fun <reified T : Feature<T>> observe(): Flow<T> = observe(T::class.java)

  /**
   * Observes changes to the specified [Feature] type. This returns a cold [Flow] that will emit the
   * current option of the feature and continue to emit new options whenever the feature's value
   * changes. The emission source (local or remote storage) is determined by the feature's
   * configured source: if a remote source is set for the feature and a remote storage is configured
   * in this Laboratory, the flow will reflect changes from the appropriate storage.
   */
  public fun <T : Feature<T>> observe(feature: Class<T>): Flow<T> =
    @OptIn(ExperimentalCoroutinesApi::class)
    localStorage
      .observeSource(feature)
      .flatMapLatest { selectedSource ->
        getStorage(feature, selectedSource).observeOption(feature)
      }
      .map { selectedOption -> getOption(feature, selectedOption) }
      .distinctUntilChanged()

  /**
   * Returns the current option of the specified [Feature]. If the feature has a remote source and a
   * remote storage is configured, this function will automatically fetch the option from the remote
   * storage when the feature's source is set to remote (and from local storage otherwise).
   */
  public suspend inline fun <reified T : Feature<T>> experiment(): T = experiment(T::class.java)

  /**
   * Returns the current option of the specified [Feature]. If the feature has a remote source and a
   * remote storage is configured, this function will automatically fetch the option from the remote
   * storage when the feature's source is set to remote (and from local storage otherwise).
   */
  public suspend fun <T : Feature<T>> experiment(feature: Class<T>): T {
    val selectedSource = localStorage.getSource(feature)
    val selectedOption = getStorage(feature, selectedSource).getOption(feature)
    return getOption(feature, selectedOption)
  }

  /**
   * Checks whether the given feature [option] is currently active. This function obtains the
   * feature's current option (from the appropriate source) and compares it to [option]. It returns
   * `true` if the feature is presently set to [option], or `false` otherwise.
   */
  public suspend fun <T : Feature<T>> experimentIs(option: T): Boolean {
    @Suppress("UNCHECKED_CAST")
    return experiment(option::class.java as Class<T>) == option
  }

  /**
   * Sets the specified [Feature] [option] as the active option.
   *
   * **Note:** This operation affects only the local storage; any configured remote storage is left
   * unchanged.
   *
   * The result indicates whether the value was stored successfully.
   */
  public suspend fun <T : Feature<T>> setOption(option: T): Boolean =
    localStorage.setOptions(option)

  /**
   * Sets multiple [Feature] [options] at once. If [options] contains more than one option for the
   * same feature flag, the last one in the vararg array is applied.
   *
   * **Note:** This operation affects only the local storage; any configured remote storage is left
   * unchanged.
   *
   * The result indicates whether the batch was stored successfully.
   */
  public suspend fun <T : Feature<*>> setOptions(vararg options: T): Boolean =
    localStorage.setOptions(options.toSet())

  /**
   * Sets multiple [Feature] [options] at once. If [options] contains more than one option for the
   * same feature flag, the last one in the collection is applied.
   *
   * **Note:** This operation affects only the local storage; any configured remote storage is left
   * unchanged.
   *
   * The result indicates whether the batch was stored successfully.
   */
  public suspend fun <T : Feature<*>> setOptions(options: Collection<T>): Boolean =
    localStorage.setOptions(options)

  /**
   * Clears all stored feature flag options from the local storage. After this call, all features
   * will appear as if they are set to their default options (unless a remote source provides
   * another value).
   *
   * **Note:** This operation affects only the local storage; any configured remote storage is left
   * unchanged.
   *
   * The result indicates whether the operation was performed successfully.
   */
  public suspend fun clear(): Boolean = localStorage.clear()

  private fun <T : Feature<T>> getStorage(
    feature: Class<T>,
    selectedSource: Feature.Source?,
  ): OptionStorage {
    return when (selectedSource) {
      Feature.Source.Local -> localStorage
      Feature.Source.Remote -> remoteStorage
      null -> remoteStorage?.takeIf { getSource(feature).isRemote }
    } ?: localStorage
  }

  private fun <T : Feature<T>> getSource(feature: Class<T>): Feature.Source =
    sourceFactory.create(feature.firstOption)

  private fun <T : Feature<T>> getOption(feature: Class<T>, selectedOption: T?): T =
    selectedOption ?: optionFactory.create(feature)

  public companion object {
    /**
     * Creates a [Laboratory] with an in-memory persistence mechanism for feature flags. This
     * convenience method sets up a Laboratory that uses only local in-memory storage (no remote
     * source).
     */
    public fun inMemory(): Laboratory = create(Storage.inMemory())

    /**
     * Creates a [Laboratory] with a provided [storage]. The given [storage] will serve as the local
     * storage backend for all feature flags.
     */
    public fun create(storage: Storage): Laboratory = builder().localStorage(storage).build()

    /**
     * Creates a builder that allows customization of the [Laboratory] instance. A local feature
     * storage via [LocalStorageStep.localStorage] must be specified at minimum. Optionally, a
     * remote storage and other configuration options may be specified before calling
     * [BuildingStep.build] to create the [Laboratory].
     */
    public fun builder(): LocalStorageStep = Builder()
  }

  internal class Builder : LocalStorageStep, BuildingStep {
    lateinit var localStorage: Storage

    override fun localStorage(storage: Storage): BuildingStep = apply { localStorage = storage }

    var remoteStorage: Storage? = null

    override fun remoteStorage(storage: Storage): BuildingStep = apply { remoteStorage = storage }

    var defaultSourceFactory: DefaultSourceFactory? = null

    override fun defaultSourceFactory(factory: DefaultSourceFactory): BuildingStep = apply {
      defaultSourceFactory = factory
    }

    var defaultOptionFactory: DefaultOptionFactory? = null

    override fun defaultOptionFactory(factory: DefaultOptionFactory): BuildingStep = apply {
      defaultOptionFactory = factory
    }

    override fun build(): Laboratory = Laboratory(this)
  }

  /** The first step when building a [Laboratory] that requires setting [localStorage]. */
  public interface LocalStorageStep {
    /**
     * Sets the local feature storage to be used by the [Laboratory]. If provided, this storage will
     * be used to read feature flag options for any features that declare a local source.
     */
    public fun localStorage(storage: Storage): BuildingStep
  }

  /**
   * The final step of the fluent builder that allows setting optional parameters and then creating
   * the [Laboratory].
   */
  public interface BuildingStep {
    /**
     * Sets an optional remote feature storage for this [Laboratory]. If provided, this storage will
     * be used to read feature flag options for any features that declare a remote source. If not
     * called, the [Laboratory] will function with local storage only.
     */
    public fun remoteStorage(storage: Storage): BuildingStep

    /**
     * Sets a factory to provide override default sources for features that have remote sources. A
     * [DefaultSourceFactory] can specify which source (local or remote) should be considered the
     * default for a given feature when no source has been explicitly selected. This is optional; if
     * not set, each feature flag with sources will default to its defined [Feature.defaultSource].
     */
    public fun defaultSourceFactory(factory: DefaultSourceFactory): BuildingStep

    /**
     * Sets a factory to provide override default options for features. The [DefaultOptionFactory]
     * can supply alternative default values for feature flags beyond their built-in defaults, which
     * this [Laboratory] will use when no value is set. This is optional; if not set, each feature
     * flag with sources will default to its defined [Feature.defaultOption].
     */
    public fun defaultOptionFactory(factory: DefaultOptionFactory): BuildingStep

    /** Creates a new [Laboratory] instance with the configuration from this builder. */
    public fun build(): Laboratory
  }
}
