package io.mehow.laboratory.inspector

@JvmInline
internal value class QueryString private constructor(private val value: String) {
  fun isEmpty() = value.isEmpty()

  fun isNotEmpty() = value.isNotEmpty()

  val length
    get() = value.length

  operator fun get(index: Int) = value[index]

  operator fun contains(other: QueryString) =
    when {
      other.isEmpty() -> true
      this.isEmpty() -> false
      else -> {
        value.asSequence().runningFold(0, accumulateMatchingLength(other)).any { matchingLength ->
          matchingLength == other.length
        }
      }
    }

  companion object {
    val Empty = QueryString("")

    fun create(value: String): QueryString {
      val cleanValue = buildString {
        for (character in value) {
          if (character.isLetterOrDigit()) {
            append(character.lowercase())
          }
        }
      }
      return QueryString(cleanValue)
    }
  }
}

private fun accumulateMatchingLength(other: QueryString) = { index: Int, char: Char ->
  if (index < other.length && char == other[index]) {
    index + 1
  } else {
    index
  }
}
