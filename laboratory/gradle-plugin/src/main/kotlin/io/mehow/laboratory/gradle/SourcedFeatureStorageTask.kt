package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.FeatureFlagOption
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal abstract class SourcedFeatureStorageTask @Inject constructor(
  objects: ObjectFactory,
) : OutputTask() {
  @Input @Optional
  val storage: Property<SourcedFeatureStorageInput?> = objects.property(SourcedFeatureStorageInput::class.java)

  @Input
  val features: ListProperty<FeatureFlagInput> = objects.listProperty(FeatureFlagInput::class.java)

  @OutputDirectory
  override val outputDirectory: DirectoryProperty = objects.directoryProperty()

  @TaskAction
  fun generateSourcedFeatureStorage() {
    outputDirectory.get().asFile.deleteRecursively()
    storage.orNull
      ?.toModel(features.get().flatMap(FeatureFlagInput::toModelsWithChildren).sourceNames().distinct())
      ?.prepare()
      ?.writeTo(outputDirectory.get().asFile)
  }

  private fun List<FeatureFlagModel>.sourceNames(): List<String> = mapNotNull(FeatureFlagModel::source)
    .map(FeatureFlagModel::options)
    .flatMap(List<FeatureFlagOption>::toList)
    .map(FeatureFlagOption::name)
}
