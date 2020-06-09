package io.mehow.laboratory.inspector

internal class FeatureGroup(
  val name: String,
  val models: List<FeatureModel>
) {
  val hasFeatures = models.isNotEmpty()
}
