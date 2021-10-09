// TODO: Copied from https://github.com/kotest/kotest-extensions-arrow
//   Delete after https://github.com/kotest/kotest/issues/2437
package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import io.kotest.assertions.assertionCounter
import io.kotest.assertions.collectOrThrow
import io.kotest.assertions.eq.eq
import io.kotest.assertions.errorCollector
import io.kotest.matchers.Matcher
import io.kotest.matchers.equalityMatcher
import io.kotest.matchers.invokeMatcher
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal infix fun <A> A.shouldBe(a: A): A =
  when (a) {
    is Matcher<*> -> invokeMatcher(this, a as Matcher<A>)
    else -> {
      val actual = this
      assertionCounter.inc()
      eq(actual, a)?.let(errorCollector::collectOrThrow)
      actual
    }
  }

internal infix fun <A> A.shouldNotBe(a: A?): A =
  when (a) {
    is Matcher<*> -> invokeMatcher(this, (a as Matcher<A>).invert())
    else -> {
      invokeMatcher(this, equalityMatcher(a).invert())
      this
    }
  }

/**
 * smart casts to [Either.Right] and fails with [failureMessage] otherwise.
 * ```kotlin
 * import arrow.core.Either.Right
 * import arrow.core.Either
 * import io.kotest.assertions.arrow.core.shouldBeRight
 *
 * fun main() {
 *   //sampleStart
 *   fun squared(i: Int): Int = i * i
 *   val result = squared(5)
 *   val either = Either.conditionally(result == 25, { result }, { 25 })
 *   val a = either.shouldBeRight { "5 * 5 == 25 but was $it " }
 *   val smartCasted: Right<Int> = either
 *   //sampleEnd
 *   println(smartCasted)
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
public fun <A, B> Either<A, B>.shouldBeRight(failureMessage: (A) -> String = { "Expected Either.Right, but found Either.Left with value $it" }): B {
  contract {
    returns() implies (this@shouldBeRight is Right<B>)
  }
  return when (this) {
    is Right -> value
    is Left -> throw AssertionError(failureMessage(value))
  }
}

public infix fun <A, B> Either<A, B>.shouldBeRight(b: B): B =
  shouldBeRight().shouldBe(b)

public infix fun <A, B> Either<A, B>.shouldNotBeRight(b: B): B =
  shouldBeRight().shouldNotBe(b)

/**
 * smart casts to [Either.Left] and fails with [failureMessage] otherwise.
 * ```kotlin
 * import arrow.core.Either.Left
 * import arrow.core.Either
 * import io.kotest.assertions.arrow.core.shouldBeLeft
 *
 * fun main() {
 *   //sampleStart
 *   val either = Either.conditionally(false, { "Always false" }, { throw RuntimeException("Will never execute") })
 *   val a = either.shouldBeLeft()
 *   val smartCasted: Left<String> = either
 *   //sampleEnd
 *   println(smartCasted)
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
public fun <A, B> Either<A, B>.shouldBeLeft(failureMessage: (B) -> String = { "Expected Either.Left, but found Either.Right with value $it" }): A {
  contract {
    returns() implies (this@shouldBeLeft is Left<A>)
  }
  return when (this) {
    is Left -> value
    is Right -> throw AssertionError(failureMessage(value))
  }
}

public infix fun <A, B> Either<A, B>.shouldBeLeft(a: A): A =
  shouldBeLeft().shouldBe(a)

public infix fun <A, B> Either<A, B>.shouldNotBeLeft(a: A): A =
  shouldBeLeft().shouldNotBe(a)
