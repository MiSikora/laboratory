package io.mehow.laboratory.gradle

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * Applies the Laboratory Gradle plugin to a project.
 *
 * When applied, the plugin:
 * - Registers the `laboratory` extension used for configuration.
 * - Configures code generation tasks for feature flags and related helpers.
 *
 * The Kotlin Gradle plugin must be applied before this plugin.
 */
public class LaboratoryPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create(PluginName, LaboratoryExtension::class.java)

    target.checkKotlinPlugin()
    target.setUpProject(extension)
  }

  private fun Project.checkKotlinPlugin() {
    val hasKotlin =
      with(plugins) {
        hasPlugin("org.jetbrains.kotlin.jvm") || hasPlugin("org.jetbrains.kotlin.android")
      }
    check(hasKotlin) { "Laboratory Gradle plugin applied in '$path' requires Kotlin plugin." }
  }

  private fun Project.setUpProject(extension: LaboratoryExtension) {
    addLaboratoryDependency()

    val hasAndroid = plugins.hasPlugin("com.android.base")
    registerFeatureFlagsTask(extension, hasAndroid)
    registerFeatureFactoryTask(extension, hasAndroid)
    registerOptionFactoryTask(extension, hasAndroid)
  }

  private fun Project.addLaboratoryDependency() {
    dependencies.add("api", "io.mehow.laboratory:laboratory:$LibraryVersion")
  }

  private fun Project.registerFeatureFlagsTask(
    extension: LaboratoryExtension,
    hasAndroid: Boolean,
  ) {
    registerOutputTask<FeatureFlagsTask>("generateFeatureFlags", hasAndroid) { task ->
      task.group = PluginName
      task.description = "Generate feature flags"
      task.inputFlags.set(extension.featureInputs)
      task.outputDirectory.set(layout.buildDirectory.dir("generated/laboratory/code/feature-flags"))
    }
  }

  private fun Project.registerFeatureFactoryTask(
    extension: LaboratoryExtension,
    hasAndroid: Boolean,
  ) {
    registerOutputTask<FeatureFactoryTask>("generateFeatureFactory", hasAndroid) { task ->
      task.group = PluginName
      task.description = "Generate feature factory"
      task.factory.set(extension.factoryInput)
      task.features.set(
        extension.factoryFeatureInputs.getValue(DependencyContribution.FeatureFactory)
      )
      task.outputDirectory.set(
        layout.buildDirectory.dir("generated/laboratory/code/feature-factory")
      )
    }
  }

  private fun Project.registerOptionFactoryTask(
    extension: LaboratoryExtension,
    hasAndroid: Boolean,
  ) {
    registerOutputTask<OptionFactoryTask>("generateOptionFactory", hasAndroid) { task ->
      task.group = PluginName
      task.description = "Generate option factory"
      task.factory.set(extension.optionFactoryInput)
      task.features.set(
        extension.factoryFeatureInputs.getValue(DependencyContribution.OptionFactory)
      )
      task.outputDirectory.set(
        layout.buildDirectory.dir("generated/laboratory/code/option-factory")
      )
    }
  }

  private inline fun <reified T : OutputTask> Project.registerOutputTask(
    name: String,
    hasAndroid: Boolean,
    crossinline action: (T) -> Unit,
  ): TaskProvider<T> {
    val task = tasks.register(name, T::class.java) { action(it) }
    makeKotlinDependOnTask(task)
    contributeToSourceSets(task, hasAndroid)
    return task
  }

  private fun Project.makeKotlinDependOnTask(task: TaskProvider<out Task>) {
    tasks.withType(KotlinJvmCompile::class.java).configureEach { kotlinTask ->
      kotlinTask.dependsOn(task)
    }
  }

  private fun Project.contributeToSourceSets(
    task: TaskProvider<out OutputTask>,
    hasAndroid: Boolean,
  ) =
    if (hasAndroid) {
      contributeToAndroid(task)
    } else {
      contributeToKotlin(task)
    }

  private fun Project.contributeToKotlin(task: TaskProvider<out OutputTask>) {
    val sourceSets = extensions.getByType(KotlinSourceSetContainer::class.java).sourceSets
    val kotlinSourceSet = sourceSets.getByName("main").kotlin
    kotlinSourceSet.srcDir(task)
  }

  private fun Project.contributeToAndroid(task: TaskProvider<out OutputTask>) {
    extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
      // 'kotlin' sources do not include
      variant.sources.java?.addGeneratedSourceDirectory(task, OutputTask::outputDirectory)
    }
  }
}
