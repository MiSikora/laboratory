package io.mehow.laboratory.inspector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal inline fun <reified T> Iterable<Flow<T>>.combineLatest() = combine(this) { it.toList() }

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

internal fun Iterable<String>.containsAllInOrder(other: Iterable<String>) = containsAllInOrder(other) { text, query ->
  text.contains(query, ignoreCase = true)
}
