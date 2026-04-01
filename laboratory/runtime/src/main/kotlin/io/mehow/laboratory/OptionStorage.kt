package io.mehow.laboratory

import io.mehow.laboratory.internal.InternalLaboratoryApi
import io.mehow.laboratory.internal.options
import io.mehow.laboratory.internal.optionsRaw
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists and retrieves feature options using a key-value [Storage]. Options are stored by name
 * and resolved via feature enum.
 */
public open class OptionStorage internal constructor(protected val storage: Storage) {
  /** Gets the selected option for the feature or `null` if unset. */
  public suspend fun <T> getOption(feature: Class<out T>): T?
    where T : Feature<T>, T : Enum<out T> =
    storage.getString(feature.storageKey)?.let(feature::findOption)

  /** Observes changes to the feature's selected option. */
  public fun <T> observeOption(feature: Class<out T>): Flow<T?>
    where T : Feature<T>, T : Enum<out T> =
    storage.stringFlow(feature.storageKey).map { value -> value?.let(feature::findOption) }

  /** Stores a feature options. */
  public suspend fun setOption(option: Feature<*>): Boolean =
    storage.setString(option.storageKey, option.name)

  /** Stores one or more feature options. */
  public suspend fun setOptions(option: Feature<*>, vararg options: Feature<*>): Boolean {
    val entries = buildMap {
      put(option.storageKey, option.name)
      for (option in options) {
        put(option.storageKey, option.name)
      }
    }
    return storage.setStrings(entries)
  }

  /** Stores a collection of feature options. */
  public suspend fun setOptions(options: Collection<Feature<*>>): Boolean {
    val entries = options.associate { option -> option.storageKey to option.name }
    return storage.setStrings(entries)
  }

  /** Clears all stored feature options. */
  public suspend fun clear(): Boolean = storage.clear()
}

/** Extension of [OptionStorage] that also manages feature source selection. */
public class SourceOptionStorage internal constructor(storage: Storage) : OptionStorage(storage) {
  /** Gets the selected source for the feature or `null` if unset. */
  public suspend fun <T> getSource(feature: Class<out T>): Feature.Source?
    where T : Feature<T>, T : Enum<out T> =
    storage.getBoolean(feature.storageKey)?.let(Boolean::toFeatureSource)

  /** Observes changes to the feature's source. */
  public fun <T> observeSource(feature: Class<out T>): Flow<Feature.Source?>
    where T : Feature<T>, T : Enum<out T> =
    storage.booleanFlow(feature.storageKey).map { value -> value?.let(Boolean::toFeatureSource) }

  /** Sets the feature source to local. */
  public suspend inline fun <reified T> setLocalSource(): Boolean
    where T : Feature<T>, T : Enum<out T> = setLocalSource(T::class.java)

  /** Sets the feature source to local. */
  public suspend fun setLocalSource(feature: Class<out Feature<*>>): Boolean =
    storage.setBoolean(feature.storageKey, false)

  /** Sets the feature source to remote. */
  public suspend inline fun <reified T> setRemoteSource(): Boolean
    where T : Feature<T>, T : Enum<out T> = setRemoteSource(T::class.java)

  /** Sets the feature source to remote. */
  public suspend fun setRemoteSource(feature: Class<out Feature<*>>): Boolean =
    storage.setBoolean(feature.storageKey, true)
}

internal class RawOptionStorage(private val storage: Storage) {
  fun observeOption(feature: Class<out Feature<*>>): Flow<Feature<*>?> =
    storage.stringFlow(feature.storageKey).map { value -> value?.let(feature::findOption) }

  fun observeSource(feature: Class<out Feature<*>>): Flow<Feature.Source?> =
    storage.booleanFlow(feature.storageKey).map { value -> value?.let(Boolean::toFeatureSource) }
}

@OptIn(InternalLaboratoryApi::class)
private fun <T> Class<out T>.findOption(name: String): T? where T : Feature<T>, T : Enum<out T> =
  options.firstOrNull { option ->
    option.name == name
  }

@OptIn(InternalLaboratoryApi::class)
private fun Class<out Feature<*>>.findOption(name: String): Feature<*>? =
  optionsRaw.firstOrNull { option ->
    option.name == name
  }

private val Class<out Feature<*>>.storageKey
  get() = name

private val Feature<*>.storageKey
  get() = javaClass.storageKey

private fun Boolean.toFeatureSource() = if (this) Feature.Source.Remote else Feature.Source.Local
