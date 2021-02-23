package io.mehow.laboratory

import kotlinx.coroutines.runBlocking

/**
 * A blocking equivalent of [Laboratory].
 */
public class BlockingLaboratory internal constructor(
  private val laboratory: Laboratory,
) {
  /**
   * Returns the current option of the input [Feature]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public inline fun <reified T : Feature<T>> experiment(): T = experiment(T::class.java)

  /**
   * Returns the current option of the input [Feature]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<T>> experiment(feature: Class<T>): T = runBlocking { laboratory.experiment(feature) }

  /**
   * Checks if a [Feature] is set to the input [option]. Warning – this call can block the calling thread.
   *
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<T>> experimentIs(option: T): Boolean = runBlocking { laboratory.experimentIs(option) }

  /**
   * Sets a [Feature] to have the input [option]. Warning – this call can block the calling thread.
   *
   * @return `true` if the option was set successfully, `false` otherwise.
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<*>> setOption(option: T): Boolean = runBlocking { laboratory.setOption(option) }

  /**
   * Sets [Features][Feature] to have the input [options]. If [options] contains more than one option
   * for the same feature flag, the last one should be applied. Warning – this call can block the calling thread.
   *
   * @return `true` if the option was set successfully, `false` otherwise.
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun <T : Feature<*>> setOptions(vararg options: T): Boolean = runBlocking { laboratory.setOptions(*options) }

  /**
   * Removes all stored feature flag options. Warning – this call can block the calling thread.
   *
   * @return `true` if the option was set successfully, `false` otherwise.
   * @see BlockingIoCall
   */
  @BlockingIoCall
  public fun clear(): Boolean = runBlocking { laboratory.clear() }
}
