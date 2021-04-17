package io.mehow.laboratory.generator

import arrow.core.Either

public class Supervisor internal constructor(
  internal val featureFlag: FeatureFlagModel,
  internal val option: FeatureFlagOption,
) {
  public data class Builder(
    private val featureFlag: FeatureFlagModel,
    private val option: FeatureFlagOption,
  ) {
    internal fun build(): Either<GenerationFailure, Supervisor> = Either.conditionally(
        featureFlag.options.map(FeatureFlagOption::name).contains(option.name),
        { NoMatchingOptionFound(featureFlag.toString(), option.name) },
        { Supervisor(featureFlag, option) },
    )
  }
}
