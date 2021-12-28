package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import io.mehow.laboratory.generator.Visibility.Internal

public class SourcedFeatureStorageModel(
  internal val className: ClassName,
  internal val sourceNames: List<String>,
  internal val visibility: Visibility = Internal,
) {
  public fun prepare(): FileSpec = SourcedFeatureStorageGenerator(this).fileSpec()
}
