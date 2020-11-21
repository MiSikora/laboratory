package io.mehow.laboratory.inspector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

internal fun <T> Iterable<Flow<T>>.observeElements(): Flow<List<T>> {
  val emptyFlow = emptyFlow<List<T>>()
  return fold(emptyFlow) { xs, x ->
    if (xs === emptyFlow) {
      x.map(::listOf)
    } else {
      xs.combine(x) { a, b -> a + b }
    }
  }
}
