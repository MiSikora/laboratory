package io.mehow.laboratory.generator

import arrow.core.Nel

interface GenerationFailure {
  val message: String
}

data class InvalidPackageName(
  private val fqcn: String
) : GenerationFailure {
  override val message
    get() = "Invalid package name for $fqcn."
}

data class InvalidFeatureName(
  private val name: String,
  private val fqcn: String
) : GenerationFailure {
  override val message
    get() = "" +
        "Feature name must contain only alphanumeric characters or underscores " +
        "and must start with a letter. Found $name in $fqcn."
}

data class NoFeatureValues(
  private val fqcn: String
) : GenerationFailure {
  override val message
    get() = "$fqcn feature must have at least one value."
}

data class InvalidFeatureValues(
  private val invalidValues: Nel<String>,
  private val fqcn: String
) : GenerationFailure {
  override val message
    get() = "" +
        "Feature values must contain only alphanumeric characters or underscores " +
        "and must start with a letter. Found ${invalidValues.toList()} in $fqcn."
}

data class FeatureValuesCollision(
  private val collisions: Nel<String>,
  private val fqcn: String
) : GenerationFailure {
  override val message: String
    get() = "Found feature values collision for $fqcn: ${collisions.toList()}."
}

data class FeaturesCollision(
  private val collisions: Nel<String>
) : GenerationFailure {
  override val message: String
    get() = "Found feature collisions: ${collisions.toList()}."

  companion object {
    fun fromFeatures(models: Nel<FeatureFlagModel>): FeaturesCollision {
      return FeaturesCollision(models.map { @Kt41142 it.fqcn })
    }
  }
}
