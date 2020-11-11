package io.mehow.laboratory.inspector

import io.mehow.laboratory.Feature

internal data class OptionUiModel(
  val option: Feature<*>,
  val isSelected: Boolean,
)
