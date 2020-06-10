package io.mehow.laboratory.compiler

import arrow.core.Either
import arrow.core.extensions.fx
import io.mehow.laboratory.compiler.Visiblity.Internal
import java.io.File

class FeatureFactoryModel private constructor(
  internal val visibility: Visiblity,
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
    internal val visibility: Visiblity = Internal,
    internal val packageName: String = "",
    internal val features: List<FeatureFlagModel>
  ) {
    internal val name = "GeneratedFeatureFactory"
    internal val fqcn = if (packageName.isEmpty()) name else "$packageName.$name"

    fun build(): Either<CompilationFailure, FeatureFactoryModel> {
      return Either.fx {
        val packageName = !validatePackageName()
        val flags = !features.checkForDuplicates(::FlagNamespaceCollision)
        FeatureFactoryModel(visibility, packageName, name, flags)
      }
    }

    private fun validatePackageName(): Either<CompilationFailure, String> {
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
