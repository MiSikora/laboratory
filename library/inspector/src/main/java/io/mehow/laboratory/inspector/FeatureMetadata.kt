package io.mehow.laboratory.inspector

import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class FeatureMetadata private constructor(private val feature: Class<Feature<*>>) {
  val simpleReadableName = feature.name.substringAfterLast('.').replace('$', '.')

  val values = feature.enumConstants?.toList<Feature<*>>().orEmpty()

  val sourceMetadata = values.firstOrNull()?.sourcedWith?.let(FeatureMetadata::create)

  val defaultValue = values.firstOrNull { it.isDefaultValue } ?: values.first()

  fun observeGroup(laboratory: Laboratory): Flow<FeatureGroup> {
    val featureEmissions = observeModels(laboratory)
    val sourceEmissions = sourceMetadata?.observeModels(laboratory) ?: flowOf(emptyList())
    return featureEmissions.combine(sourceEmissions) { features, sources ->
      FeatureGroup(simpleReadableName, feature.name, features, sources)
    }
  }

  fun observeModels(laboratory: Laboratory) = laboratory.observe(feature).map { selectedFeature ->
    values.map { value -> FeatureModel(value, isSelected = selectedFeature == value) }
  }

  class Provider(
    private val featureFactories: Map<String, FeatureFactory>,
  ) {
    operator fun get(groupName: String) = featureFactories
      .mapValues { (_, factory) -> factory.create() }
      .getValue(groupName)
      .mapNotNull(FeatureMetadata::create)

    fun featuresAndSources() = featureFactories.values
      .map(FeatureFactory::create)
      .flatten()
      .mapNotNull(FeatureMetadata::create)
      .flatMap { metadata -> listOfNotNull(metadata, metadata.sourceMetadata) }
  }

  companion object {
    fun create(feature: Class<Feature<*>>) = feature
      .takeUnless { it.enumConstants.isNullOrEmpty() }
      ?.let(::FeatureMetadata)
  }
}
