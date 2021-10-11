package io.mehow.laboratory.gradle

import arrow.core.identity
import arrow.core.traverseEither
import io.mehow.laboratory.generator.FeatureFlagModel.Builder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

public open class OptionFactoryTask : DefaultTask() {
  @get:Internal internal lateinit var factory: OptionFactoryInput
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File

  @TaskAction public fun generateSourcedFeatureStorage() {
    val featureModels = features.flatMap(FeatureFlagInput::toBuilders).traverseEither(Builder::build).fold(
        ifLeft = { failure -> error(failure.message) },
        ifRight = ::identity
    )

    factory.toBuilder(featureModels).build().fold(
        ifLeft = { failure -> error(failure.message) },
        ifRight = { optionFactoryModel ->
          codeGenDir.deleteRecursively()
          optionFactoryModel.prepare().writeTo(codeGenDir)
        }
    )
  }
}
