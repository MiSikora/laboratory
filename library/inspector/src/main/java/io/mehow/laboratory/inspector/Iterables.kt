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

internal fun <T> Iterable<T>.containsAllInOrder(
  other: Iterable<T>,
  predicate: (left: T, right: T) -> Boolean,
): Boolean {
  val allCombinations = asSequence()
      .mapIndexed(::Pair)
      .flatMap { left -> other.mapIndexed { index, right -> left to (index to right) } }
  val uniqueFinds = allCombinations
      .filter { (left, right) -> predicate(left.second, right.second) }
      .map { (_, right) -> right }
      .distinct()
      .map { (_, value) -> value }
  return uniqueFinds.toList() == other.toList()
}
