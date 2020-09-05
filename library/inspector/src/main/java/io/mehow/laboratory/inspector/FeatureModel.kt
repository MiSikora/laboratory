package io.mehow.laboratory.inspector

import io.mehow.laboratory.Feature

internal class FeatureModel(
  val feature: Feature<*>,
  val isSelected: Boolean,
) {
  fun select() = FeatureModel(feature, true)
}
