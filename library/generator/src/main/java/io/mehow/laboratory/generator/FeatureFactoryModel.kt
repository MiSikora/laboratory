package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.extensions.fx
import com.squareup.kotlinpoet.ClassName
import java.io.File

class FeatureFactoryModel private constructor(
  internal val visibility: Visibility,
  internal val className: ClassName,
  internal val features: List<FeatureFlagModel>,
) {
  internal val packageName = className.packageName
  internal val name = className.simpleName

  fun generate(functionName: String, file: File): File {
    FeatureFactoryGenerator(this, functionName).generate(file)
    val outputDir = file.toPath().resolve(packageName.replace(".", "/")).toFile()
    return File(outputDir, "$name.kt")
  }

  data class Builder(
    internal val visibility: Visibility,
    internal val packageName: String,
    internal val features: List<FeatureFlagModel>,
  ) {
    fun build(name: String): Either<GenerationFailure, FeatureFactoryModel> {
      val fqcn = if (packageName.isEmpty()) name else "$packageName.$name"
      return Either.fx {
        val packageName = !validatePackageName(fqcn)
        val name = !validateName(fqcn, name)
        val features = !features.checkForDuplicates { @Kt41142 FeaturesCollision.fromFeatures(it) }
        FeatureFactoryModel(visibility, ClassName(packageName, name), features)
      }
    }

    private fun validatePackageName(fqcn: String): Either<GenerationFailure, String> {
      return Either.cond(
        test = packageName.isEmpty() || packageName.matches(packageNameRegex),
        ifTrue = { packageName },
        ifFalse = { InvalidPackageName(fqcn) }
      )
    }

    private fun validateName(fqcn: String, name: String): Either<GenerationFailure, String> {
      return Either.cond(
        test = name.matches(nameRegex),
        ifTrue = { name },
        ifFalse = { InvalidFactoryName(name, fqcn) }
      )
    }

    private companion object {
      val packageNameRegex = """^(?:[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)(?:\.[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)*${'$'}""".toRegex()
      val nameRegex = """^[a-zA-Z][a-zA-Z_\d]*""".toRegex()
    }
  }
}
