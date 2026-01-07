package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
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
    requireNoDuplicates().forEach { model -> model.prepare().writeTo(outputPath) }
  }

  private fun requireNoDuplicates(): List<FeatureFlagModel> {
    val models = inputFlags.get().map(FeatureFlagInput::toModel)
    val groupedModels = models.groupBy(FeatureFlagModel::className)
    require(models.size == groupedModels.size) {
      val duplicates =
        groupedModels
          .filterValues { it.size > 1 }
          .mapValues { (_, models) -> models.map { it.className.canonicalName } }
          .values
          .flatten()
          .distinct()
          .sorted()
      "Feature flags must have unique fully qualified names. Found following duplicates: $duplicates"
    }
    return models
  }
}
