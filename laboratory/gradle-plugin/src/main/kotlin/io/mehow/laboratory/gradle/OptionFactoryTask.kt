package io.mehow.laboratory.gradle

import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class OptionFactoryTask @Inject constructor(objects: ObjectFactory) :
  OutputTask() {
  @Input
  @Optional
  val factory: Property<OptionFactoryInput> = objects.property(OptionFactoryInput::class.java)

  @Input
  val features: ListProperty<FeatureFlagInput> = objects.listProperty(FeatureFlagInput::class.java)

  @OutputDirectory override val outputDirectory: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun generateSourcedFeatureStorage() {
    outputDirectory.get().asFile.deleteRecursively()
    factory.orNull
      ?.toModel(features.get().map(FeatureFlagInput::toModel))
      ?.prepare()
      ?.writeTo(outputDirectory.get().asFile)
  }
}
