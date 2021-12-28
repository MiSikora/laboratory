package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.mehow.laboratory.generator.Visibility.Internal

public class FeatureFactoryModel(
  internal val className: ClassName,
  internal val features: List<FeatureFlagModel>,
  internal val visibility: Visibility = Internal,
) {
  public fun prepare(functionName: String): FileSpec = FeatureFactoryGenerator(this, functionName).fileSpec()
}
