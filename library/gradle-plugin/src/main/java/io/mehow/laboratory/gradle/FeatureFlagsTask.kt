package io.mehow.laboratory.gradle

import arrow.core.Either
import io.mehow.laboratory.generator.buildAll
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class FeatureFlagsTask : DefaultTask() {
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File

  @TaskAction fun generateFeatureFlags() {
    when (val buildIntent = features.map(FeatureFlagInput::toBuilder).buildAll()) {
      is Either.Left -> error(buildIntent.a.message)
      is Either.Right -> {
        codeGenDir.deleteRecursively()
        buildIntent.b.forEach { model ->
          model.generate(codeGenDir)
        }
      }
    }
  }
}
