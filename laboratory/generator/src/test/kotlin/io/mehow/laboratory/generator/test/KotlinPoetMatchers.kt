package io.mehow.laboratory.generator.test

import com.squareup.kotlinpoet.FileSpec
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

infix fun FileSpec.shouldSpecify(value: String) = assertSoftly {
  val actualLines = toString().split("\n")
  val expectedLines = value.split("\n")
  val maxSize = maxOf(actualLines.size, expectedLines.size)
  repeat(maxSize) { line ->
    withClue("Line $line does not match") {
      actualLines.getOrNull(line) shouldBe expectedLines.getOrNull(line)
    }
  }
}
