package io.mehow.laboratory.datastore

import io.mehow.laboratory.Feature

internal enum class FeatureA : Feature<FeatureA> {
  A,
  B,
  ;

  override val defaultOption get() = A
}
