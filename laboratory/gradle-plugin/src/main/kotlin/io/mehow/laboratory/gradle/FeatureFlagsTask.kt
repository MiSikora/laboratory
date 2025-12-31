package io.mehow.laboratory.gradle

import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class FeatureFlagsTask @Inject constructor(objects: ObjectFactory) :
  OutputTask() {
  @Input
  val inputFlags: ListProperty<FeatureFlagInput> =
    objects.listProperty(FeatureFlagInput::class.java)

  @OutputDirectory override val outputDirectory: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun generateFeatureFlags() {
    outputDirectory.get().asFile.deleteRecursively()
    val outputPath = outputDirectory.get().asFile
    inputFlags.get().flatMap(FeatureFlagInput::toModelsWithChildren).forEach { model ->
      model.prepare().writeTo(outputPath)
    }
  }
}
