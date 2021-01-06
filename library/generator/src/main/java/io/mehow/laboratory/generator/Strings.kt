package io.mehow.laboratory.generator

import io.mehow.laboratory.generator.TextToken.Link
import io.mehow.laboratory.generator.TextToken.Regular

private val extractLinkRegex = """\[([^\[\]]+)]\(([^()]+)\)""".toRegex()

// TODO: https://github.com/MiSikora/laboratory/issues/71
internal fun String.prepareKdocHyperlinks(): String {
  val matches = extractLinkRegex.findAll(this)
  val regularTokens = matches.toRegularTokens(this)
  val linkTokens = matches.toLinkTokens()
  val tokens = (regularTokens + linkTokens)
      .sortedBy { (_, startIndex) -> startIndex }
      .map { (token, _) -> token }
  return buildString {
    for (token in tokens) {
      token.append(this)
    }
  }
}

private sealed class TextToken {
  abstract fun append(builder: StringBuilder)

  data class Regular(private val text: String) : TextToken() {
    override fun append(builder: StringBuilder) {
      builder.append(text)
    }
  }

  data class Link(private val text: String, private val url: String) : TextToken() {
    override fun append(builder: StringBuilder) {
      builder.append('[')
      builder.append(text.replace(' ', '·'))
      builder.append(']')
      builder.append('(')
      builder.append(url)
      builder.append(')')
    }
  }
}

private fun Sequence<MatchResult>.toLinkTokens() = map { matchResult ->
  val (text, url) = matchResult.destructured
  Link(text, url) to matchResult.range.first
}

private fun Sequence<MatchResult>.toRegularTokens(text: String) = toUnmatchedRanges(text)
    .map { range -> Regular(text.substring(range)) to range.first }

private fun Sequence<MatchResult>.toUnmatchedRanges(text: String) = sequence {
  yield(Int.MIN_VALUE..0)
  yieldAll(map { it.range }.map { it.first - 1..it.last + 1 })
  yield(text.length - 1..Int.MAX_VALUE)
}.windowed(2, 1).map { (start, end) -> start.last..end.first }.filterNot { range -> range.isEmpty() }
