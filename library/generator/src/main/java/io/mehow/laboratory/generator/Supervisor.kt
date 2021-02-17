package io.mehow.laboratory.generator

import arrow.core.Either

public class Supervisor private constructor(
  internal val featureFlag: FeatureFlagModel,
  internal val option: FeatureFlagOption,
) {
  public data class Builder(
    private val featureFlag: FeatureFlagModel,
    private val option: FeatureFlagOption,
  ) {
    internal fun build(): Either<GenerationFailure, Supervisor> = Either.cond(
        test = featureFlag.options.map { @Kt41142 it.name }.contains(option.name),
        ifTrue = { Supervisor(featureFlag, option) },
        ifFalse = { NoMatchingOptionFound(featureFlag.toString(), option.name) }
    )
  }
}
