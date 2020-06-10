package io.mehow.laboratory

interface FeatureFactory {
  fun create(): Set<Class<Enum<*>>>

  companion object
}
