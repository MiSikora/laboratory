package io.mehow.laboratory.inspector

internal class DeprecationHandler(
  private val styleSelector: FeatureStyle.Selector,
  private val alignmentSelector: FeatureAlignment.Selector,
) {
  fun getPhenotype(level: DeprecationLevel) = styleSelector.select(level)

  fun getAlignment(level: DeprecationLevel) = alignmentSelector.select(level)
}
