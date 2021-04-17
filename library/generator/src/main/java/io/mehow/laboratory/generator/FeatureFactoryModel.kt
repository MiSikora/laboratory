package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.computations.either
import com.squareup.kotlinpoet.ClassName
import java.io.File

public class FeatureFactoryModel internal constructor(
  internal val visibility: Visibility,
  internal val className: ClassName,
  internal val features: List<FeatureFlagModel>,
) {
  internal val packageName = className.packageName
  internal val name = className.simpleName

  public fun generate(functionName: String, file: File): File {
    FeatureFactoryGenerator(this, functionName).generate(file)
    val outputDir = file.toPath().resolve(packageName.replace(".", "/")).toFile()
    return File(outputDir, "$name.kt")
  }

  public data class Builder(
    internal val visibility: Visibility,
    internal val packageName: String,
    internal val features: List<FeatureFlagModel>,
  ) {
    public fun build(name: String): Either<GenerationFailure, FeatureFactoryModel> {
      val fqcn = if (packageName.isEmpty()) name else "$packageName.$name"
      return either.eager {
        val packageName = validatePackageName(fqcn).bind()
        val simpleName = validateName(fqcn, name).bind()
        val features = features.checkForDuplicates(FeaturesCollision::fromFeatures).bind()
        FeatureFactoryModel(visibility, ClassName(packageName, simpleName), features)
      }
    }

    private fun validatePackageName(fqcn: String) = Either.conditionally(
        packageName.isEmpty() || packageName.matches(packageNameRegex),
        { InvalidPackageName(fqcn) },
        { packageName },
    )

    private fun validateName(fqcn: String, name: String) = Either.conditionally(
        name.matches(nameRegex),
        { InvalidFactoryName(name, fqcn) },
        { name },
    )

    private companion object {
      val packageNameRegex = """^(?:[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)(?:\.[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)*${'$'}""".toRegex()
      val nameRegex = """^[a-zA-Z][a-zA-Z_\d]*""".toRegex()
    }
  }
}
