package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class FeatureSourceFactoryTask @Inject constructor(objects: ObjectFactory) :
  OutputTask() {
  @Input
  @Optional
  val factory: Property<FeatureFactoryInput> = objects.property(FeatureFactoryInput::class.java)

  @Input
  val features: ListProperty<FeatureFlagInput> = objects.listProperty(FeatureFlagInput::class.java)

  @OutputDirectory override val outputDirectory: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun generateFeatureFactory() {
    outputDirectory.get().asFile.deleteRecursively()
    factory.orNull
      ?.toModel(
        features.get().map(FeatureFlagInput::toModel).mapNotNull(FeatureFlagModel::source),
        "GeneratedFeatureSourceFactory",
      )
      ?.prepare("featureSourceGenerated")
      ?.writeTo(outputDirectory.get().asFile)
  }
}
