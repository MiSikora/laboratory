package io.mehow.laboratory

interface FeatureStorage {
  fun <T : Enum<*>> getFeatureName(group: Class<T>): String?
  fun <T : Enum<*>> setFeature(feature: T)

  companion object {
    fun inMemory() = object : FeatureStorage {
      private val features = mutableMapOf<String, String>()

      override fun <T : Enum<*>> getFeatureName(group: Class<T>) = features[group.name]

      override fun <T : Enum<*>> setFeature(feature: T) {
        features[feature.javaClass.name] = feature.name
      }
    }
  }
}
