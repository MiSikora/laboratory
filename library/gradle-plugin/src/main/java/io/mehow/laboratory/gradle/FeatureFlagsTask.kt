package io.mehow.laboratory.gradle

import arrow.core.traverseEither
import io.mehow.laboratory.generator.FeatureFlagModel.Builder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

public open class FeatureFlagsTask : DefaultTask() {
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File

  @TaskAction public fun generateFeatureFlags() {
    features.flatMap(FeatureFlagInput::toBuilders).traverseEither(Builder::build).fold(
        ifLeft = { failure -> error(failure.message) },
        ifRight = { featureFlagModels ->
          codeGenDir.deleteRecursively()
          for (model in featureFlagModels) {
            model.prepare().writeTo(codeGenDir)
          }
        }
    )
  }
}
