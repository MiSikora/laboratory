package io.mehow.laboratory.inspector

import java.util.Locale

internal class SearchQuery(input: String) {
  private val parts = input.replace(whiteSpaceRegex, " ")
      .split(' ')
      .flatMap { it.replace(searchNoiseRegex, "").splitToParts() }
      .map { it.lowercase(Locale.ROOT) }
  private val joinedParts = parts.joinToString("")

  fun isNotEmpty() = parts.isNotEmpty()

  fun matches(text: List<String>) = text.containsAllInOrder(parts)

  fun matches(text: String): Boolean {
    val cleanText = text.replace(searchNoiseRegex, "").replace(whiteSpaceRegex, " ")
    return cleanText.contains(joinedParts, ignoreCase = true) || cleanText.containsParts(parts)
  }

  private fun String.containsParts(parts: List<String>) = splitToParts().containsAllInOrder(parts)

  private fun String.splitToParts() = fold(emptyList<String>()) { xs, x ->
    when {
      xs.isEmpty() -> xs + x.toString()
      x.isLowerCase() && !x.isDigit() -> xs.dropLast(1) + (xs.last() + x)
      else -> xs + x.toString()
    }
  }

  override fun equals(other: Any?) = other is SearchQuery && this.parts == other.parts

  override fun hashCode() = parts.hashCode()

  override fun toString() = "SearchQuery(joinedParts='$joinedParts')"

  companion object {
    private val whiteSpaceRegex = """\s""".toRegex()
    private val searchNoiseRegex = """[.$]""".toRegex()

    val Empty = SearchQuery("")
  }
}
