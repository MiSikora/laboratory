package io.mehow.laboratory.gradle

import arrow.core.identity
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.buildAll
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

public open class FeatureFactoryTask : DefaultTask() {
  @get:Internal internal lateinit var factory: FeatureFactoryInput
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File
  @get:Internal internal lateinit var factoryClassName: String
  @get:Internal internal lateinit var factoryFunctionName: String
  @get:Internal internal lateinit var featureModelsMapper: (List<FeatureFlagModel>) -> List<FeatureFlagModel>

  @TaskAction public fun generateFeatureFactory() {
    val featureModels = features.flatMap(FeatureFlagInput::toBuilders).buildAll().fold(
        ifLeft = { failure -> error(failure.message) },
        ifRight = ::identity
    ).let(featureModelsMapper)

    factory.toBuilder(featureModels).build(factoryClassName).fold(
        ifLeft = { failure -> error(failure.message) },
        ifRight = { featureFactoryModel ->
          codeGenDir.deleteRecursively()
          featureFactoryModel.generate(factoryFunctionName, codeGenDir)
        }
    )
  }
}
