package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.right
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import java.io.File

public class SourcedFeatureStorageModel internal constructor(
  internal val visibility: Visibility,
  internal val className: ClassName,
  internal val sourceNames: List<String>,
) {
  @Deprecated("This method will be removed in 1.0.0. Use prepare instead.")
  public fun generate(file: File): File {
    prepare().writeTo(file)
    val outputDir = file.toPath().resolve(className.packageName.replace(".", "/")).toFile()
    return File(outputDir, "${className.simpleName}.kt")
  }

  public fun prepare(): FileSpec = SourcedFeatureStorageGenerator(this).fileSpec()

  public data class Builder(
    private val visibility: Visibility,
    private val className: ClassName,
    internal val sourceNames: List<String>,
  ) {
    public fun build(): Either<GenerationFailure, SourcedFeatureStorageModel> = SourcedFeatureStorageModel(
        visibility,
        className,
        sourceNames,
    ).right()
  }
}
