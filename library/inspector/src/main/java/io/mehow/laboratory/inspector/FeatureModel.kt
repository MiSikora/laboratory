package io.mehow.laboratory.inspector

internal class FeatureModel(
  val feature: Enum<*>,
  val isSelected: Boolean,
) {
  fun select() = FeatureModel(feature, true)
}
