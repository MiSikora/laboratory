package io.mehow.laboratory.inspector

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mehow.laboratory.inspector.TextToken.Link
import io.mehow.laboratory.inspector.TextToken.Regular
import org.junit.Test

class TextTokenTest {

  @Test
  fun `empty text`() {
    "".tokenize().shouldBeEmpty()
  }

  @Test
  fun `blank text`() {
    "   ".tokenize() shouldContainExactly listOf(Regular("   "))
  }

  @Test
  fun `simple text`() {
    "Hello".tokenize() shouldContainExactly listOf(Regular("Hello"))
  }

  @Test
  fun `link text`() {
    "[Hello](https://mehow.io)".tokenize() shouldContainExactly
      listOf(Link("Hello", "https://mehow.io"))
  }

  @Test
  fun `prefix text with link`() {
    "Hello [there](https://github.com/MiSikora/)".tokenize() shouldContainExactly
      listOf(Regular("Hello "), Link("there", "https://github.com/MiSikora/"))
  }

  @Test
  fun `postfix text with link`() {
    "[General](https://google.com) Kenobi".tokenize() shouldContainExactly
      listOf(Link("General", "https://google.com"), Regular(" Kenobi"))
  }

  @Test
  fun `text with link`() {
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

  @Test
  fun `multiple links`() {
    val input = "[One,](https://one.com)[ Two](https://two.com)[, Three](https://three.com)"
    input.tokenize() shouldContainExactly
      listOf(
        Link("One,", "https://one.com"),
        Link(" Two", "https://two.com"),
        Link(", Three", "https://three.com"),
      )
  }

  @Test
  fun `malformed links`() {
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
