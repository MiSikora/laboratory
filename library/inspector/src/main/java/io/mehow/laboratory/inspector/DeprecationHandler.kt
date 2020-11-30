package io.mehow.laboratory.inspector

internal class DeprecationHandler(
  private val phenotypeSelector: DeprecationPhenotype.Selector,
  private val alignmentSelector: DeprecationAlignment.Selector,
) {
  fun getPhenotype(level: DeprecationLevel) = phenotypeSelector.select(level)

  fun getAlignment(level: DeprecationLevel) = alignmentSelector.select(level)
}
