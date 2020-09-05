package io.mehow.laboratory

// Declare as Comparable in order to enforce correct generic parameter.
interface Feature<T> : Comparable<T> where T : Enum<T>, T : Feature<T> {
  val name: String
  val isFallbackValue: Boolean
}
