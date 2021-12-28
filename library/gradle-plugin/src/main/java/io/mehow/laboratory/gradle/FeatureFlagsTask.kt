package io.mehow.laboratory.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

public open class FeatureFlagsTask : DefaultTask() {
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File

  @TaskAction public fun generateFeatureFlags() {
    val featureFlagModels = features.flatMap(FeatureFlagInput::toModels)

    for (model in featureFlagModels) {
      model.prepare().writeTo(codeGenDir)
    }
  }
}
