package io.mehow.laboratory.gradle

import arrow.core.identity
import arrow.core.traverseEither
import io.mehow.laboratory.generator.FeatureFlagModel.Builder
import io.mehow.laboratory.generator.sourceNames
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

public open class SourcedFeatureStorageTask : DefaultTask() {
  @get:Internal internal lateinit var storage: SourcedFeatureStorageInput
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File

  @TaskAction public fun generateSourcedFeatureStorage() {
    val sourceNames = features.flatMap(FeatureFlagInput::toBuilders).traverseEither(Builder::build).fold(
        ifLeft = { failure -> throw GradleException(failure.message) },
        ifRight = ::identity
    ).sourceNames().distinct()

    storage.toBuilder(sourceNames).build().fold(
        ifLeft = { failure -> throw GradleException(failure.message) },
        ifRight = { sourcedFeatureStorageModel ->
          codeGenDir.deleteRecursively()
          sourcedFeatureStorageModel.prepare().writeTo(codeGenDir)
        }
    )
  }
}
