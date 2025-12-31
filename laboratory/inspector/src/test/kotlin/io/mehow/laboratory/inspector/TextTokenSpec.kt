package io.mehow.laboratory.inspector

import io.kotest.core.spec.style.FunSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mehow.laboratory.inspector.TextToken.Link
import io.mehow.laboratory.inspector.TextToken.Regular

class TextTokenSpec :
  FunSpec({
    test("can be empty") { "".tokenize().shouldBeEmpty() }

    test("can be blank") { "   ".tokenize() shouldContainExactly listOf(Regular("   ")) }

    test("can have regular text") {
      "Hello".tokenize() shouldContainExactly listOf(Regular("Hello"))
    }

    test("can have a link") {
      "[Hello](https://mehow.io)".tokenize() shouldContainExactly
        listOf(Link("Hello", "https://mehow.io"))
    }

    test("can start with regular text followed by a link") {
      "Hello [there](https://github.com/MiSikora/)".tokenize() shouldContainExactly
        listOf(Regular("Hello "), Link("there", "https://github.com/MiSikora/"))
    }

    test("can start with a link followed by a regular text") {
      "[General](https://google.com) Kenobi".tokenize() shouldContainExactly
        listOf(Link("General", "https://google.com"), Regular(" Kenobi"))
    }

    test("can have multiple regular texts and links") {
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

    test("can have multiple consecutive links") {
      val input = "[One,](https://one.com)[ Two](https://two.com)[, Three](https://three.com)"
      input.tokenize() shouldContainExactly
        listOf(
          Link("One,", "https://one.com"),
          Link(" Two", "https://two.com"),
          Link(", Three", "https://three.com"),
        )
    }

    test("ignores malformed link syntax") {
      forAll(
        row("[One[](https://one.com)"),
        row("[One](https://one.com()"),
        row("[](https://one.com"),
        row("[One]()"),
        row("[One]((https://one.com)"),
        row("[O]ne](https://one.com)"),
        row("[One](h(ttps://one.com)"),
      ) {
        it.tokenize() shouldContainExactly listOf(Regular(it))
      }
    }
  })
