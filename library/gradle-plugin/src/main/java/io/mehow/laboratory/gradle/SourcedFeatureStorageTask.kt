package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.FeatureFlagOption
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

public open class SourcedFeatureStorageTask : DefaultTask() {
  @get:Internal internal lateinit var storage: SourcedFeatureStorageInput
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File

  @TaskAction public fun generateSourcedFeatureStorage() {
    val sourceNames = features.flatMap(FeatureFlagInput::toModels).sourceNames().distinct()
    val storageModel = storage.toModel(sourceNames)
    codeGenDir.deleteRecursively()
    storageModel.prepare().writeTo(codeGenDir)
  }

  private fun List<FeatureFlagModel>.sourceNames(): List<String> = mapNotNull(FeatureFlagModel::source)
      .map(FeatureFlagModel::options)
      .flatMap { it.toList() }
      .map(FeatureFlagOption::name)
}
