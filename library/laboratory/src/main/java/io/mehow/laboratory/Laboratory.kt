package io.mehow.laboratory

class Laboratory(private val storage: FeatureStorage) {
  inline fun <reified T : Enum<T>> experiment() = experiment(T::class.java)

  fun <T : Enum<T>> experiment(group: Class<T>): T {
    val features = group.enumConstants!!
    require(features.isNotEmpty()) { "${group.name} must have at least one value" }
    val defaultFeature = features.first()
    val expectedName = storage.getFeatureName(defaultFeature.javaClass) ?: defaultFeature.name
    return features.firstOrNull { it.name == expectedName } ?: defaultFeature
  }

  fun <T : Enum<T>> setFeature(feature: T) {
    storage.setFeature(feature)
  }

  companion object {
    fun inMemory() = Laboratory(FeatureStorage.inMemory())
  }
}
