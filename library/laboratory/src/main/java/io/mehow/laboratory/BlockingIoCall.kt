package io.mehow.laboratory

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * Opt-in annotation denoting that the used function can potentially block a calling thread with I/O operations.
 */
@RequiresOptIn(
    message = "" +
        "Used API can block a thread with IO operations. " +
        "Either opt in to its usage or use a suspending equivalent."
)
@Retention(BINARY)
@Target(CLASS, FUNCTION)
annotation class BlockingIoCall
