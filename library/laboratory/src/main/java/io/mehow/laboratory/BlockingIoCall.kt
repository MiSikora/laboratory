package io.mehow.laboratory

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@RequiresOptIn(
  message = "" +
    "Used API can block a thread with IO operations. " +
    "Use either a suspending equivalent or opt in to its usage."
)
@Retention(BINARY)
@Target(CLASS, FUNCTION)
annotation class BlockingIoCall
