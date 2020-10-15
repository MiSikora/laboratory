package io.mehow.laboratory.gradle

import arrow.core.Either
import arrow.core.identity
import io.mehow.laboratory.generator.buildAll
import io.mehow.laboratory.generator.sourceNames
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SourcedFeatureStorageTask : DefaultTask() {
  @get:Internal internal lateinit var storage: SourcedFeatureStorageInput
  @get:Internal internal lateinit var features: List<FeatureFlagInput>
  @get:Internal internal lateinit var codeGenDir: File

  @TaskAction fun generateSourcedFeatureStorage() {
    val sourceNames = features.map(FeatureFlagInput::toBuilder).buildAll().fold(
        ifLeft = { failure -> error(failure.message) },
        ifRight = ::identity
    ).sourceNames().distinct()

    when (val buildIntent = storage.toBuilder(sourceNames).build()) {
      is Either.Left -> error(buildIntent.a.message)
      is Either.Right -> {
        codeGenDir.deleteRecursively()
        buildIntent.b.generate(codeGenDir)
      }
    }
  }
}
