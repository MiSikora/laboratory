package io.mehow.laboratory.inspector

internal data class FeatureGroup(
  val name: String,
  val fqcn: String,
  val models: List<FeatureModel>,
) {
  val hasFeatures = models.isNotEmpty()
}
