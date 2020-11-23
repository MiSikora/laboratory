package io.mehow.laboratory.inspector

import java.util.Locale

internal class SearchQuery(input: String) {
  private val parts = input.replace(whiteSpaceRegex, " ")
      .split(' ')
      .flatMap { it.replace(searchNoiseRegex, "").splitToParts() }
      .map { it.toLowerCase(Locale.ROOT) }
  private val joinedParts = parts.joinToString("")

  fun isNotEmpty() = parts.isNotEmpty()

  fun matches(text: List<String>) = text.map { it.toLowerCase(Locale.ROOT) }
      .containsAllInOrder(parts) { textPart, queryPart -> queryPart in textPart }

  fun matches(text: String): Boolean {
    val cleanText = text.replace(searchNoiseRegex, "").replace(whiteSpaceRegex, " ")
    return joinedParts in cleanText.toLowerCase(Locale.ROOT) || cleanText.containsParts(parts)
  }

  private fun String.containsParts(parts: List<String>) = splitToParts()
      .map { it.toLowerCase(Locale.ROOT) }
      .toList()
      .containsAllInOrder(parts) { textPart, queryPart -> queryPart in textPart }

  private fun String.splitToParts() = fold(emptyList<String>()) { xs, x ->
    when {
      xs.isEmpty() -> xs + x.toString()
      x.isLowerCase() && !x.isDigit() -> xs.dropLast(1) + (xs.last() + x)
      else -> xs + x.toString()
    }
  }

  override fun equals(other: Any?) = other is SearchQuery && this.parts == other.parts

  override fun hashCode() = parts.hashCode()

  companion object {
    private val whiteSpaceRegex = """\s""".toRegex()
    private val searchNoiseRegex = """[.$]""".toRegex()

    val Empty = SearchQuery("")
  }
}
