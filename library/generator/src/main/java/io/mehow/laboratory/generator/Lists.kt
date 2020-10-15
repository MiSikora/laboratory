package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.Nel
import arrow.core.identity

internal fun <T> Iterable<T>.findDuplicates(): Set<T> {
  return groupingBy(::identity).eachCount().filterValues { it > 1 }.keys
}

internal fun <T> List<T>.checkForDuplicates(
  failureProvider: (Nel<T>) -> GenerationFailure,
): Either<GenerationFailure, List<T>> {
  val duplicates = findDuplicates()
  return Nel.fromList(duplicates.toList())
      .toEither { this }
      .swap()
      .mapLeft(failureProvider)
}
