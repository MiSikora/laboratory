package io.mehow.laboratory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists and retrieves feature options using a key-value [Storage]. Options are stored by name
 * and resolved via feature enum.
 */
public open class OptionStorage internal constructor(protected val storage: Storage) {
  /** Gets the selected option for the feature or `null` if unset. */
  public suspend fun <T : Feature<out T>> getOption(feature: Class<out T>): T? =
    storage.getString(feature.storageKey)?.let(feature::findOption)

  /** Observes changes to the feature's selected option. */
  public fun <T : Feature<out T>> observeOption(feature: Class<out T>): Flow<T?> =
    storage.stringFlow(feature.storageKey).map { value -> value?.let(feature::findOption) }

  /** Stores one or more feature options. */
  public suspend fun <T : Feature<*>> setOptions(option: T, vararg options: T): Boolean {
    val entries = buildMap {
      put(option.storageKey, option.name)
      for (option in options) {
        put(option.storageKey, option.name)
      }
    }
    return storage.setStrings(entries)
  }

  /** Stores a collection of feature options. */
  public suspend fun <T : Feature<*>> setOptions(options: Collection<T>): Boolean {
    val entries = options.associate { option -> option.storageKey to option.name }
    return storage.setStrings(entries)
  }

  /** Clears all stored feature options. */
  public suspend fun clear(): Boolean = storage.clear()
}

/** Extension of [OptionStorage] that also manages feature source selection. */
public class SourceOptionStorage internal constructor(storage: Storage) : OptionStorage(storage) {
  /** Gets the selected source for the feature or `null` if unset. */
  public suspend fun <T : Feature<out T>> getSource(feature: Class<out T>): Feature.Source? =
    storage.getBoolean(feature.storageKey)?.let(Boolean::toFeatureSource)

  /** Observes changes to the feature's source. */
  public fun <T : Feature<out T>> observeSource(feature: Class<out T>): Flow<Feature.Source?> =
    storage.booleanFlow(feature.storageKey).map { value -> value?.let(Boolean::toFeatureSource) }

  /** Sets the feature source to local. */
  public suspend inline fun <reified T : Feature<out T>> setLocalSource(): Boolean =
    setLocalSource(T::class.java)

  /** Sets the feature source to local. */
  public suspend fun <T : Feature<out T>> setLocalSource(feature: Class<out T>): Boolean =
    storage.setBoolean(feature.storageKey, false)

  /** Sets the feature source to remote. */
  public suspend inline fun <reified T : Feature<out T>> setRemoteSource(): Boolean =
    setRemoteSource(T::class.java)

  /** Sets the feature source to remote. */
  public suspend fun <T : Feature<out T>> setRemoteSource(feature: Class<out T>): Boolean =
    storage.setBoolean(feature.storageKey, true)
}

private fun <T : Feature<out T>> Class<out T>.findOption(value: String) =
  options.firstOrNull { option -> option.name == value }

private val Class<out Feature<*>>.storageKey
  get() = name

private val Feature<*>.storageKey
  get() = javaClass.storageKey

private fun Boolean.toFeatureSource() = if (this) Feature.Source.Remote else Feature.Source.Local
