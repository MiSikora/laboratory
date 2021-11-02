package io.mehow.laboratory.inspector

import app.cash.turbine.FlowTurbine
import kotlinx.coroutines.withTimeout

internal suspend fun <T> FlowTurbine<T>.awaitItemEventually(
  timeoutMs: Long = this.timeoutMs,
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
