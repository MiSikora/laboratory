package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.mehow.laboratory.generator.Visibility.Internal

public class SourcedFeatureStorageModel(
  public val className: ClassName,
  public val sourceNames: List<String>,
  public val visibility: Visibility = Internal,
) {
  public fun prepare(): FileSpec = SourcedFeatureStorageGenerator(this).fileSpec()
}
