package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.sourceNames
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
}
