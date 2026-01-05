package io.mehow.laboratory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * An interface representing a basic key-value storage for strings and booleans.
 *
 * This storage interface supports storing and retrieving values by keys. The same key can be used
 * to store both strings and booleans. This means that the code below should work.
 *
 * ```
 * storage.setString("key", "test")
 * storage.setBoolean("key", true)
 *
 * assertEquals("test", storage.getString("key"))
 * assertEquals(true, storage.getBoolean("key"))
 * ```
 *
 * Being an abstraction, concrete implementations of `Storage` may vary in how data is persisted
 * (in-memory, on disk, etc.), but they should all honor the contract of this API. In particular,
 * implementations should ensure thread-safety and that any changes (via setting values or clearing)
 * trigger emissions on the corresponding [Flow]s. Updating an entry with the same value should not
 * trigger emissions.
 */
public interface Storage {
  /** Retrieves the string value associated with [key], or `null` if none is stored. */
  public suspend fun getString(key: String): String?

  /**
   * Returns a [Flow] that emits updates for the string value associated with [key].
   *
   * The returned [Flow] will emit the current value of [key] immediately when it is collected.
   * After that, it emits a new value whenever the string for [key] changes. If no value is
   * currently stored for [key], the flow will emit `null` as the initial value. Subsequent
   * emissions reflect new values set for that key, or `null` if the value is removed.
   */
  public fun stringFlow(key: String): Flow<String?>

  /**
   * Stores a string [value] under the given [key].
   *
   * This function saves the provided [value] into the storage, associating it with the specified
   * [key]. If there was a previous value for [key], it will be overwritten.
   *
   * The result indicates whether the value was stored successfully.
   */
  public suspend fun setString(key: String, value: String): Boolean

  /**
   * Stores multiple string values in a single batch operation.
   *
   * The [entries] map contains key-value pairs to be stored. Each entry's key will be associated
   * with its corresponding string value in the storage. This method allows batch updates, which can
   * be more efficient than calling [setString] repeatedly, and may be executed atomically.
   *
   * The result indicates whether the batch was stored successfully.
   */
  public suspend fun setStrings(entries: Map<String, String>): Boolean

  /** Retrieves the boolean value associated with [key], or `null` if none is stored. */
  public suspend fun getBoolean(key: String): Boolean?

  /**
   * Returns a [Flow] that emits updates for the boolean value associated with [key].
   *
   * The returned [Flow] will emit the current value of [key] immediately when it is collected.
   * After that, it emits a new value whenever the boolean for [key] changes. If no value is
   * currently stored for [key], the flow will emit `null` as the initial value. Subsequent
   * emissions reflect new values set for that key, or `null` if the value is removed.
   */
  public fun booleanFlow(key: String): Flow<Boolean?>

  /**
   * Stores a boolean [value] under the given [key].
   *
   * This function saves the provided [value] into the storage, associating it with the specified
   * [key]. If there was a previous value for [key], it will be overwritten.
   *
   * The result indicates whether the value was stored successfully.
   */
  public suspend fun setBoolean(key: String, value: Boolean): Boolean

  /**
   * Stores multiple boolean values in a single batch operation.
   *
   * The [entries] map contains key-value pairs to be stored. Each entry's key will be associated
   * with its corresponding boolean value in the storage. This method allows batch updates, which
   * can be more efficient than calling [setBoolean] repeatedly, and may be executed atomically.
   *
   * The result indicates whether the batch was stored successfully.
   */
  public suspend fun setBooleans(entries: Map<String, Boolean>): Boolean

  /**
   * Removes all key-value entries from the storage.
   *
   * Clears the entire storage of all stored entries. After this operation, the storage will be
   * empty. Any [Flow] obtained via [stringFlow] or [booleanFlow] for any key should emit `null`
   * after the clear, indicating that those keys no longer have values.
   *
   * The result indicates whether the operation was performed successfully
   */
  public suspend fun clear(): Boolean

  public companion object {
    /**
     * Creates a new in-memory [Storage] instance.
     *
     * The returned [Storage] keeps all data in memory and does not persist anything to disk. Each
     * invocation returns a fresh [Storage] with its own isolated data.
     */
    public fun inMemory(): Storage = InMemoryStorage()
  }
}

internal class InMemoryStorage : Storage {
  private val dataFlow = MutableStateFlow(StoredData())

  override suspend fun getString(key: String): String? {
    return dataFlow.value.strings[key]
  }

  override fun stringFlow(key: String): Flow<String?> {
    return dataFlow.map { data -> data.strings[key] }.distinctUntilChanged()
  }

  override suspend fun setString(key: String, value: String): Boolean {
    dataFlow.update { data -> data.copy(strings = data.strings + (key to value)) }
    return true
  }

  override suspend fun setStrings(entries: Map<String, String>): Boolean {
    dataFlow.update { data -> data.copy(strings = data.strings + entries) }
    return true
  }

  override suspend fun getBoolean(key: String): Boolean? {
    return dataFlow.value.booleans[key]
  }

  override fun booleanFlow(key: String): Flow<Boolean?> {
    return dataFlow.map { data -> data.booleans[key] }.distinctUntilChanged()
  }

  override suspend fun setBoolean(key: String, value: Boolean): Boolean {
    dataFlow.update { data -> data.copy(booleans = data.booleans + (key to value)) }
    return true
  }

  override suspend fun setBooleans(entries: Map<String, Boolean>): Boolean {
    dataFlow.update { data -> data.copy(booleans = data.booleans + entries) }
    return true
  }

  override suspend fun clear(): Boolean {
    dataFlow.update { StoredData() }
    return true
  }
}

private data class StoredData(
  val strings: Map<String, String> = emptyMap(),
  val booleans: Map<String, Boolean> = emptyMap(),
)
