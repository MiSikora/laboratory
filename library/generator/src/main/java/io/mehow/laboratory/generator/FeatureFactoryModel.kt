package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.extensions.fx
import java.io.File

class FeatureFactoryModel private constructor(
  internal val visibility: Visibility,
  internal val packageName: String,
  internal val name: String,
  internal val features: List<FeatureFlagModel>
) {
  fun generate(file: File): File {
    FeatureFactoryGenerator(this).generate(file)
    val outputDir = file.toPath().resolve(packageName.replace(".", "/")).toFile()
    return File(outputDir, "$name.kt")
  }

  data class Builder(
    internal val visibility: Visibility,
    internal val packageName: String,
    internal val features: List<FeatureFlagModel>
  ) {
    internal val name = "GeneratedFeatureFactory"
    internal val fqcn = if (packageName.isEmpty()) name else "$packageName.$name"

    fun build(): Either<GenerationFailure, FeatureFactoryModel> {
      return Either.fx {
        val packageName = !validatePackageName()
        val features = !features.checkForDuplicates { @Kt41142 FeaturesCollision.fromFeatures(it) }
        FeatureFactoryModel(visibility, packageName, name, features)
      }
    }

    private fun validatePackageName(): Either<GenerationFailure, String> {
      return Either.cond(
        test = packageName.isEmpty() || packageName.matches(packageNameRegex),
        ifTrue = { packageName },
        ifFalse = { InvalidPackageName(fqcn) }
      )
    }

    private companion object {
      val packageNameRegex =
        """^(?:[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)(?:\.[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)*${'$'}""".toRegex()
    }
  }
}
