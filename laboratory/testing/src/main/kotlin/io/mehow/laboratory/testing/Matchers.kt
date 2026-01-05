package io.mehow.laboratory.testing

import io.kotest.matchers.collections.shouldHaveSingleElement

infix fun <T> Iterable<T>.shouldHaveSingle(predicate: (T) -> Boolean): T {
  shouldHaveSingleElement(predicate)
  return single(predicate)
}
