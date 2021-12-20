package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.right
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

public class FeatureFactoryModel internal constructor(
  internal val visibility: Visibility,
  internal val className: ClassName,
  internal val features: List<FeatureFlagModel>,
) {
  public fun prepare(functionName: String): FileSpec = FeatureFactoryGenerator(this, functionName).fileSpec()

  public data class Builder(
    private val visibility: Visibility,
    private val className: ClassName,
    private val features: List<FeatureFlagModel>,
  ) {
    public fun build(): Either<GenerationFailure, FeatureFactoryModel> = FeatureFactoryModel(
        visibility,
        className,
        features,
    ).right()
  }
}
