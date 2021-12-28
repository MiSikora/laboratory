package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.mehow.laboratory.generator.Visibility.Internal

public class FeatureFactoryModel(
  public val className: ClassName,
  public val features: List<FeatureFlagModel>,
  public val visibility: Visibility = Internal,
) {
  public fun prepare(functionName: String): FileSpec = FeatureFactoryGenerator(this, functionName).fileSpec()
}
