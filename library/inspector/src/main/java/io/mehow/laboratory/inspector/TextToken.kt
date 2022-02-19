package io.mehow.laboratory.inspector

import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.SpannedString
import android.text.style.URLSpan
import android.widget.TextView
import io.mehow.laboratory.inspector.TextToken.Link
import io.mehow.laboratory.inspector.TextToken.Regular

internal sealed class TextToken {
  abstract fun buildSpan(builder: SpannableStringBuilder)

  data class Regular(private val text: String) : TextToken() {
    override fun buildSpan(builder: SpannableStringBuilder) {
      builder.append(text)
    }
  }

  data class Link(private val text: String, private val url: String) : TextToken() {
    override fun buildSpan(builder: SpannableStringBuilder) {
      val linkStart = builder.length
      builder.append(text)
      builder.setSpan(URLSpan(url), linkStart, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
    }
  }
}

private val extractLinkRegex = """\[([^\[\]]+)]\(([^()]+)\)""".toRegex()

internal fun String.tokenize(): List<TextToken> {
  val matches = extractLinkRegex.findAll(this)
  val regularTokens = matches.toRegularTokens(this)
  val linkTokens = matches.toLinkTokens()
  return (regularTokens + linkTokens)
      .sortedBy { (_, startIndex) -> startIndex }
      .map { (token, _) -> token }
      .toList()
}

private fun Sequence<MatchResult>.toLinkTokens() = map { matchResult ->
  val (text, url) = matchResult.destructured
  Link(text, url) to matchResult.range.first
}

private fun Sequence<MatchResult>.toRegularTokens(text: String) = toUnmatchedRanges(text)
    .map { range -> Regular(text.substring(range)) to range.first }

private fun Sequence<MatchResult>.toUnmatchedRanges(text: String) = sequence {
  yield(Int.MIN_VALUE..0)
  yieldAll(map(MatchResult::range).map { it.first - 1..it.last + 1 })
  yield(text.length - 1..Int.MAX_VALUE)
}.windowed(2, 1).map { (start, end) -> start.last..end.first }.filterNot { range -> range.isEmpty() }

internal fun TextView.setTextTokens(tokens: Iterable<TextToken>) {
  text = SpannableStringBuilder().apply {
    for (token in tokens) {
      token.buildSpan(this)
    }
  }.let(::SpannedString)
}
