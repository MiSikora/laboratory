package io.mehow.laboratory.inspector

import app.cash.turbine.TurbineTestContext
import kotlinx.coroutines.withTimeout

suspend fun <T> TurbineTestContext<T>.awaitItemEventually(
  timeoutMs: Long = 1000L,
  assertion: (T) -> Unit,
) = withTimeout(timeoutMs) {
  while (true) {
    val isMatch = try {
      assertion(awaitItem())
      true
    } catch (_: Throwable) {
      false
    }
    if (isMatch) break
  }
}
