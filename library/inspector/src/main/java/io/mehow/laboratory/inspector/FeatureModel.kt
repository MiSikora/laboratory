package io.mehow.laboratory.inspector

import io.mehow.laboratory.Feature

internal data class FeatureModel(
  val feature: Feature<*>,
  val isSelected: Boolean,
) {
  fun select() = FeatureModel(feature, true)
}
