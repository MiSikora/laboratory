package io.mehow.laboratory

interface FeatureFactory {
  fun create(): Set<Class<Feature<*>>>

  companion object
}
