package io.mehow.laboratory

class Laboratory(private val storage: FeatureStorage) {
  suspend inline fun <reified T : Feature<T>> experiment() = experiment(T::class.java)

  suspend fun <T : Feature<T>> experiment(group: Class<T>): T {
    val features = group.enumConstants!!
    require(features.isNotEmpty()) { "${group.name} must have at least one value" }
    val defaultFeature = features.firstOrNull { it.isFallbackValue } ?: features.first()
    val expectedName = storage.getFeatureName(defaultFeature.javaClass) ?: defaultFeature.name
    return features.firstOrNull { it.name == expectedName } ?: defaultFeature
  }

  suspend fun <T : Feature<*>> setFeature(feature: T) = storage.setFeature(feature)

  companion object {
    fun inMemory() = Laboratory(FeatureStorage.inMemory())
  }
}
