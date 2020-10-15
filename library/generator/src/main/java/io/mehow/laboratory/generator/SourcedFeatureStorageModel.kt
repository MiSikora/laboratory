package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.extensions.fx
import com.squareup.kotlinpoet.ClassName
import java.io.File

class SourcedFeatureStorageModel private constructor(
  internal val visibility: Visibility,
  internal val className: ClassName,
  internal val sourceNames: List<String>,
) {
  internal val packageName = className.packageName
  internal val name = className.simpleName

  fun generate(file: File): File {
    SourcedFeatureStorageGenerator(this).generate(file)
    val outputDir = file.toPath().resolve(packageName.replace(".", "/")).toFile()
    return File(outputDir, "$name.kt")
  }

  data class Builder(
    internal val visibility: Visibility,
    internal val packageName: String,
    internal val sourceNames: List<String>,
  ) {
    internal val name = "sourcedGeneratedFeatureStorage"
    internal val fqcn = if (packageName.isEmpty()) name else "$packageName.$name"

    fun build(): Either<GenerationFailure, SourcedFeatureStorageModel> {
      return Either.fx {
        val packageName = !validatePackageName()
        SourcedFeatureStorageModel(visibility, ClassName(packageName, name), sourceNames)
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
      val packageNameRegex = """^(?:[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)(?:\.[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)*${'$'}""".toRegex()
    }
  }
}
