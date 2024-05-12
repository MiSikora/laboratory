package io.mehow.laboratory.datastore

import io.mehow.laboratory.Feature

enum class FeatureA : Feature<FeatureA> {
  A,
  B,
  ;

  override val defaultOption get() = A
}
