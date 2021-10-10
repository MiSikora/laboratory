package io.mehow.laboratory

internal class SafeDefaultOptionFactory(
  private val delegate: DefaultOptionFactory,
) {
  fun <T : Feature<out T>> create(feature: Class<out T>): T? {
    val defaultOption = delegate.create(feature.firstOption) ?: return null
    check(defaultOption::class.java == feature) {
      val optionName = "${defaultOption::class.java.simpleName}.$defaultOption"
      val featureName = feature.canonicalName
      "Tried to use $optionName as a default option for $featureName"
    }
    @Suppress("UNCHECKED_CAST")
    return defaultOption as T
  }
}
