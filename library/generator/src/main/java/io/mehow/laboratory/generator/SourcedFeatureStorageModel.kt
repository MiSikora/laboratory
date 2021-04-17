package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.computations.either
import com.squareup.kotlinpoet.ClassName
import java.io.File

public class SourcedFeatureStorageModel internal constructor(
  internal val visibility: Visibility,
  className: ClassName,
  internal val sourceNames: List<String>,
) {
  internal val packageName = className.packageName
  internal val name = className.simpleName

  public fun generate(file: File): File {
    SourcedFeatureStorageGenerator(this).generate(file)
    val outputDir = file.toPath().resolve(packageName.replace(".", "/")).toFile()
    return File(outputDir, "$name.kt")
  }

  public data class Builder(
    internal val visibility: Visibility,
    internal val packageName: String,
    internal val sourceNames: List<String>,
  ) {
    internal val name = "SourcedGeneratedFeatureStorage"
    internal val fqcn = if (packageName.isEmpty()) name else "$packageName.$name"

    public fun build(): Either<GenerationFailure, SourcedFeatureStorageModel> = either.eager {
      val packageName = validatePackageName().bind()
      SourcedFeatureStorageModel(visibility, ClassName(packageName, name), sourceNames)
    }

    private fun validatePackageName() = Either.conditionally(
        packageName.isEmpty() || packageName.matches(packageNameRegex),
        { InvalidPackageName(fqcn) },
        { packageName },
    )

    private companion object {
      val packageNameRegex = """^(?:[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)(?:\.[a-zA-Z]+(?:\d*[a-zA-Z_]*)*)*${'$'}""".toRegex()
    }
  }
}
