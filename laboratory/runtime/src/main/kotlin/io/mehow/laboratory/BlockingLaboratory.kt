package io.mehow.laboratory

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlinx.coroutines.runBlocking

/**
 * Opt-in annotation indicating that the annotated function may block the calling thread with I/O
 * operations. Use of any API marked with `@BlockingIoCall` requires explicit opt-in, either via
 * `@OptIn(BlockingIoCall::class)` on the calling code or by enabling the opt-in flag for this
 * annotation at compile time.
 *
 * This annotation is applied to blocking feature flag operations to signal that they perform
 * potentially slow, blocking work. It’s recommended to use the suspend or asynchronous alternatives
 * when possible instead of relying on blocking calls.
 */
@RequiresOptIn(
  message =
    "This API may block a thread with I/O operations. Opt-in if you intend to allow blocking or use a suspending alternative."
)
@Retention(BINARY)
@Target(FUNCTION)
public annotation class BlockingIoCall

/**
 * A blocking equivalent of [Laboratory] that provides synchronous versions of feature flag
 * operations. Each function in this class will execute the corresponding [Laboratory] suspend
 * function on the current thread, potentially performing I/O and thus **blocking** the thread. This
 * is useful in contexts where suspending functions cannot be used, but should be avoided on main/UI
 * threads.
 */
public class BlockingLaboratory internal constructor(private val laboratory: Laboratory) {
  /** The blocking equivalent of [Laboratory.experiment]. */
  @BlockingIoCall
  public inline fun <reified T> experiment(): T where T : Feature<T>, T : Enum<out T> =
    experiment(T::class.java)

  /** The blocking equivalent of [Laboratory.experiment]. */
  @BlockingIoCall
  public fun <T> experiment(feature: Class<T>): T where T : Feature<T>, T : Enum<out T> =
    runBlocking {
      laboratory.experiment(feature)
    }

  /** The blocking equivalent of [Laboratory.experimentIs]. */
  @BlockingIoCall
  public fun <T> experimentIs(option: T): Boolean where T : Feature<T>, T : Enum<out T> =
    runBlocking {
      laboratory.experimentIs(option)
    }

  /** The blocking equivalent of [Laboratory.isEnabled]. */
  @BlockingIoCall
  public inline fun <reified T> isEnabled(): Boolean
    where T : BinaryFeature<T>, T : Feature<T>, T : Enum<out T> = isEnabled(T::class.java)

  /** The blocking equivalent of [Laboratory.isEnabled]. */
  @BlockingIoCall
  public fun <T> isEnabled(feature: Class<out T>): Boolean
    where T : BinaryFeature<T>, T : Feature<T>, T : Enum<out T> = runBlocking {
    laboratory.isEnabled(feature)
  }

  /** The blocking equivalent of [Laboratory.setOption]. */
  @BlockingIoCall
  public fun setOption(option: Feature<*>): Boolean = runBlocking { laboratory.setOption(option) }

  /** The blocking equivalent of [Laboratory.setOptions]. */
  @BlockingIoCall
  public fun setOptions(vararg options: Feature<*>): Boolean = runBlocking {
    laboratory.setOptions(*options)
  }

  /** The blocking equivalent of [Laboratory.setOptions]. */
  @BlockingIoCall
  public fun setOptions(options: Collection<Feature<*>>): Boolean = runBlocking {
    laboratory.setOptions(options)
  }

  /** The blocking equivalent of [Laboratory.clear]. */
  @BlockingIoCall public fun clear(): Boolean = runBlocking { laboratory.clear() }
}
