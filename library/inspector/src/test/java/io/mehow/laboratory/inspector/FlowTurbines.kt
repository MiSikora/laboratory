package io.mehow.laboratory.inspector

import app.cash.turbine.FlowTurbine
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

internal suspend fun <T> FlowTurbine<T>.awaitItemEventually(
  timeout: Duration = this.timeout,
  assertion: (T) -> Unit,
) = withTimeout(timeout) {
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
