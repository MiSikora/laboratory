package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
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
    val featureModels = featureModelsMapper(features.flatMap(FeatureFlagInput::toModels))
    val factoryModel = factory.toModel(featureModels, factoryClassName)
    codeGenDir.deleteRecursively()
    factoryModel.prepare(factoryFunctionName).writeTo(codeGenDir)
  }
}
