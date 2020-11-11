package io.mehow.laboratory.generator

import arrow.core.Nel

public interface GenerationFailure {
  public val message: String
}

public data class InvalidPackageName(
  private val fqcn: String,
) : GenerationFailure {
  override val message: String
    get() = "Invalid package name for $fqcn."
}

public data class InvalidFeatureName(
  private val name: String,
  private val fqcn: String,
) : GenerationFailure {
  override val message: String
    get() = "" +
        "Feature flag name must contain only alphanumeric characters or underscores " +
        "and must start with a letter. Found $name in $fqcn."
}

public data class InvalidFactoryName(
  private val name: String,
  private val fqcn: String,
) : GenerationFailure {
  override val message: String
    get() = "" +
        "Factory name must contain only alphanumeric characters or underscores " +
        "and must start with a letter. Found $name in $fqcn."
}

public data class NoFeatureValues(
  private val fqcn: String,
) : GenerationFailure {
  override val message: String
    get() = "$fqcn feature flag must have at least one option."
}

public data class InvalidFeatureValues(
  private val invalidValues: Nel<String>,
  private val fqcn: String,
) : GenerationFailure {
  override val message: String
    get() = "" +
        "Feature flag options must contain only alphanumeric characters or underscores " +
        "and must start with a letter. Found ${invalidValues.toList()} in $fqcn."
}

public data class FeatureValuesCollision(
  private val collisions: Nel<String>,
  private val fqcn: String,
) : GenerationFailure {
  override val message: String
    get() = "Found feature flag options collision for $fqcn: ${collisions.toList()}."
}

public data class NoFeatureDefaultValue(
  private val fqcn: String,
) : GenerationFailure {
  override val message: String
    get() = "Feature flag must have a single default option. Found none in $fqcn."
}

public data class MultipleFeatureDefaultValues(
  private val collisions: Nel<String>,
  private val fqcn: String,
) : GenerationFailure {
  override val message: String
    get() = "Feature flag must have a single default option. Found ${collisions.toList()} in $fqcn."
}

public data class FeaturesCollision(
  private val collisions: Nel<String>,
) : GenerationFailure {
  override val message: String
    get() = "Found feature flag collisions: ${collisions.toList()}."

  public companion object {
    public fun fromFeatures(models: Nel<FeatureFlagModel>): FeaturesCollision {
      return FeaturesCollision(models.map { @Kt41142 it.reflectionName })
    }
  }
}
