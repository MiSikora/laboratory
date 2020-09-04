package io.mehow.laboratory

interface FeatureStorage {
  suspend fun <T : Enum<*>> getFeatureName(group: Class<T>): String?
  suspend fun <T : Enum<*>> setFeature(feature: T): Boolean

  companion object {
    fun inMemory() = object : FeatureStorage {
      private val features = mutableMapOf<String, String>()

      override suspend fun <T : Enum<*>> getFeatureName(group: Class<T>) = features[group.name]

      override suspend fun <T : Enum<*>> setFeature(feature: T): Boolean {
        features[feature.javaClass.name] = feature.name
        return true
      }
    }
  }
}
