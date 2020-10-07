package io.mehow.laboratory.gradle

import arrow.core.Either
import arrow.core.identity
import io.mehow.laboratory.generator.buildAll
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class FeatureFactoryTask : DefaultTask() {
  @get:Internal internal lateinit var factory: FeatureFactoryInput
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File

  @TaskAction fun generateFeatureFactory() {
    val featureModels = features.map(FeatureFlagInput::toBuilder).buildAll().fold(
      ifLeft = { failure -> error(failure.message) },
      ifRight = ::identity
    )

    when (val buildIntent = factory.toBuilder(featureModels).build("GeneratedFeatureFactory")) {
      is Either.Left -> error(buildIntent.a.message)
      is Either.Right -> {
        codeGenDir.deleteRecursively()
        buildIntent.b.generate("featureGenerated", codeGenDir)
      }
    }
  }
}
