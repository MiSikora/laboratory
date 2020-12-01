package io.mehow.laboratory.inspector

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mehow.laboratory.inspector.TextToken.Link
import io.mehow.laboratory.inspector.TextToken.Regular

internal class TextTokenSpec : DescribeSpec({
  describe("text tokens") {
    it("can be empty") {
      "".tokenize().shouldBeEmpty()
    }

    it("can be blank") {
      "   ".tokenize() shouldContainExactly listOf(Regular("   "))
    }

    it("can have regular text") {
      "Hello".tokenize() shouldContainExactly listOf(Regular("Hello"))
    }

    it("can have link") {
      "[Hello](https://mehow.io)".tokenize() shouldContainExactly listOf(Link("Hello", "https://mehow.io"))
    }

    it("can start with regular text followed by a link") {
      "Hello [there](https://github.com/MiSikora/)".tokenize() shouldContainExactly listOf(
          Regular("Hello "),
          Link("there", "https://github.com/MiSikora/"),
      )
    }

    it("can start with a link followed by a regular text") {
      "[General](https://google.com) Kenobi".tokenize() shouldContainExactly listOf(
          Link("General", "https://google.com"),
          Regular(" Kenobi"),
      )
    }

    it("can have multiple regular texts and links") {
      val input = "Hello [there](https://github.com)… [General](https://sample.org) Kenobi"
      input.tokenize() shouldContainExactly listOf(
          Regular("Hello "),
          Link("there", "https://github.com"),
          Regular("… "),
          Link("General", "https://sample.org"),
          Regular(" Kenobi"),
      )
    }

    it("can have multiple consecutive links") {
      val input = "[One,](https://one.com)[ Two](https://two.com)[, Three](https://three.com)"
      input.tokenize() shouldContainExactly listOf(
          Link("One,", "https://one.com"),
          Link(" Two", "https://two.com"),
          Link(", Three", "https://three.com"),
      )
    }

    it("does not use malformed link syntax") {
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
  }
})