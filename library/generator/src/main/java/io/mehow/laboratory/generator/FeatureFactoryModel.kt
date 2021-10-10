package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.right
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import java.io.File

public class FeatureFactoryModel internal constructor(
  internal val visibility: Visibility,
  internal val className: ClassName,
  internal val features: List<FeatureFlagModel>,
) {
  @Deprecated("This method will be removed in 1.0.0. Use prepare instead.")
  public fun generate(functionName: String, file: File): File {
    prepare(functionName).writeTo(file)
    val outputDir = file.toPath().resolve(className.packageName.replace(".", "/")).toFile()
    return File(outputDir, "${className.simpleName}.kt")
  }

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
