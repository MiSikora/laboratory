package io.mehow.laboratory.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal class LaboratoryTasks(
  private val featureFlagsTask: TaskProvider<FeatureFlagsTask>,
  private val optionFactoryTask: TaskProvider<OptionFactoryTask>,
  private val featureFactoryTask: TaskProvider<FeatureFactoryTask>,
) {
  private val outputTasks = listOf(featureFlagsTask, optionFactoryTask, featureFactoryTask)

  fun configureInput(project: Project, extension: LaboratoryExtension) {
    featureFlagsTask.configure { task ->
      task.inputFlags.set(extension.featureInputs)
      task.outputDirectory.set(project.outputDir("feature-flags"))
    }
    optionFactoryTask.configure { task ->
      task.factory.set(extension.optionFactoryInput)
      task.features.set(
        extension.factoryFeatureInputs.getValue(DependencyContribution.OptionFactory)
      )
      task.outputDirectory.set(project.outputDir("option-factory"))
    }
    featureFactoryTask.configure { task ->
      task.factory.set(extension.factoryInput)
      task.features.set(
        extension.factoryFeatureInputs.getValue(DependencyContribution.FeatureFactory)
      )
      task.outputDirectory.set(project.outputDir("feature-factory"))
    }
  }

  fun configureKotlinDependency(project: Project) {
    outputTasks.forEach(project::configureKotlinDependency)
  }

  fun contributeToSources(sources: ProjectSources) {
    outputTasks.forEach(sources::srcDir)
  }

  private fun Project.outputDir(name: String) =
    layout.buildDirectory.dir("generated/laboratory/code/$name")

  companion object {
    fun registerIn(project: Project) =
      LaboratoryTasks(
        project.registerTask<FeatureFlagsTask>("generateFeatureFlags") { task ->
          task.group = PluginName
          task.description = "Generate feature flags"
        },
        project.registerTask<OptionFactoryTask>("generateOptionFactory") { task ->
          task.group = PluginName
          task.description = "Generate option factory"
        },
        project.registerTask<FeatureFactoryTask>("generateFeatureFactory") { task ->
          task.group = PluginName
          task.description = "Generate feature factory"
        },
      )
  }
}

private inline fun <reified T : Task> Project.registerTask(
  name: String,
  crossinline action: (T) -> Unit,
) = tasks.register(name, T::class.java) { action(it) }

private fun <T : Task> Project.configureKotlinDependency(task: TaskProvider<out T>) {
  tasks.withType(KotlinJvmCompile::class.java).configureEach { kotlinTask ->
    kotlinTask.dependsOn(task)
  }
}
