package io.mehow.laboratory.inspector

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mehow.laboratory.inspector.TextToken.Link
import io.mehow.laboratory.inspector.TextToken.Regular

class TextTokenSpec : FunSpec() {
  init {
    test("empty text") { "".tokenize().shouldBeEmpty() }

    test("blank text") { "   ".tokenize() shouldContainExactly listOf(Regular("   ")) }

    test("simple text") { "Hello".tokenize() shouldContainExactly listOf(Regular("Hello")) }

    test("link text") {
      "[Hello](https://mehow.io)".tokenize() shouldContainExactly
        listOf(Link("Hello", "https://mehow.io"))
    }

    test("prefix text with link") {
      "Hello [there](https://github.com/MiSikora/)".tokenize() shouldContainExactly
        listOf(Regular("Hello "), Link("there", "https://github.com/MiSikora/"))
    }

    test("postfix text with link") {
      "[General](https://google.com) Kenobi".tokenize() shouldContainExactly
        listOf(Link("General", "https://google.com"), Regular(" Kenobi"))
    }

    test("text with link") {
      val input = "Hello [there](https://github.com)… [General](https://sample.org) Kenobi"
      input.tokenize() shouldContainExactly
        listOf(
          Regular("Hello "),
          Link("there", "https://github.com"),
          Regular("… "),
          Link("General", "https://sample.org"),
          Regular(" Kenobi"),
        )
    }

    test("multiple links") {
      val input = "[One,](https://one.com)[ Two](https://two.com)[, Three](https://three.com)"
      input.tokenize() shouldContainExactly
        listOf(
          Link("One,", "https://one.com"),
          Link(" Two", "https://two.com"),
          Link(", Three", "https://three.com"),
        )
    }

    test("malformed links") {
      val inputs =
        listOf(
          "[One[](https://one.com)",
          "[One](https://one.com()",
          "[](https://one.com",
          "[One]()",
          "[One]((https://one.com)",
          "[O]ne](https://one.com)",
          "[One](h(ttps://one.com)",
        )
      for (input in inputs) {
        input.tokenize() shouldContainExactly listOf(Regular(input))
      }
    }
  }
}
