package io.mehow.laboratory.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

public open class OptionFactoryTask : DefaultTask() {
  @get:Internal internal lateinit var factory: OptionFactoryInput
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File

  @TaskAction public fun generateSourcedFeatureStorage() {
    val featureModels = features.flatMap(FeatureFlagInput::toModels)
    val factoryModel = factory.toModel(featureModels)
    codeGenDir.deleteRecursively()
    factoryModel.prepare().writeTo(codeGenDir)
  }
}
