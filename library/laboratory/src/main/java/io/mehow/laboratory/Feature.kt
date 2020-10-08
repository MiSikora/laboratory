package io.mehow.laboratory

// Declare as Comparable in order to enforce correct generic parameter.
interface Feature<T> : Comparable<T> where T : Enum<T>, T : Feature<T> {
  val name: String
  val isDefaultValue: Boolean
  @JvmDefault val sourcedWith: Class<Feature<*>>? get() = null
}
