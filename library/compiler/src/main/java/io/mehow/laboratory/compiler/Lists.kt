package io.mehow.laboratory.compiler

import arrow.core.Either
import arrow.core.Nel

internal fun <T> Iterable<T>.findDuplicates(): Set<T> {
  return groupingBy { it }.eachCount().filterValues { it > 1 }.keys
}

internal fun <T> List<T>.checkForDuplicates(
  failureProvider: (Nel<T>) -> CompilationFailure
): Either<CompilationFailure, List<T>> {
  val duplicates = findDuplicates()
  return Nel.fromList(duplicates.toList())
    .toEither { this }
    .swap()
    .mapLeft(failureProvider)
}
