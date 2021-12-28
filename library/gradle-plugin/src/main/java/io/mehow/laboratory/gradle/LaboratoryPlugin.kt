package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.laboratoryVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

private const val pluginName = "laboratory"

public class LaboratoryPlugin : Plugin<Project> {
  private val hasAndroid = AtomicBoolean(false)
  private val hasKotlin = AtomicBoolean(false)
  private lateinit var extension: LaboratoryExtension

  override fun apply(project: Project) {
    extension = project.extensions.create(pluginName, LaboratoryExtension::class.java).apply {
      this.project = project
    }
    project.setUpKotlinProject()
    project.setUpAndroidProject()
  }

  private fun Project.setUpKotlinProject() {
    val kotlinPluginHandler = { _: Plugin<*> -> hasKotlin.set(true) }
    plugins.withId("org.jetbrains.kotlin.android", kotlinPluginHandler)
    plugins.withId("org.jetbrains.kotlin.jvm", kotlinPluginHandler)

    afterEvaluate {
      registerLaboratoryTasks(afterAndroid = false)
    }
  }

  private fun Project.setUpAndroidProject() {
    val androidPluginHandler = { _: Plugin<*> ->
      hasAndroid.set(true)
      afterEvaluate {
        registerLaboratoryTasks(afterAndroid = true)
      }
    }
    plugins.withId("com.android.application", androidPluginHandler)
    plugins.withId("com.android.library", androidPluginHandler)
    plugins.withId("com.android.instantapp", androidPluginHandler)
    plugins.withId("com.android.feature", androidPluginHandler)
    plugins.withId("com.android.dynamic-feature", androidPluginHandler)
  }

  private fun Project.registerLaboratoryTasks(afterAndroid: Boolean) {
    if (hasAndroid.get() && !afterAndroid) return

    check(hasKotlin.get()) {
      "Laboratory Gradle plugin requires Kotlin plugin."
    }

    addLaboratoryDependency()
    registerFeaturesTask()
    registerFeatureFactoryTask()
    registerSourcedFeatureStorageTask()
    registerFeatureSourcesFactoryTask()
    registerOptionFactoryTask()
  }

  private fun Project.registerFeaturesTask() = afterEvaluate {
    val codeGenDir = File("$buildDir/generated/laboratory/code/feature-flags")
    val featuresTask = registerTask<FeatureFlagsTask>("generateFeatureFlags") { task ->
      task.group = pluginName
      task.description = "Generate Laboratory features."
      task.features = extension.featureInputs
      task.codeGenDir = codeGenDir
    }
    addSourceSets(featuresTask, codeGenDir)
  }

  private fun Project.registerFeatureFactoryTask() = afterEvaluate {
    val factoryInput = extension.factoryInput ?: return@afterEvaluate

    val codeGenDir = File("${project.buildDir}/generated/laboratory/code/feature-factory")
    val factoryTask = registerTask<FeatureFactoryTask>("generateFeatureFactory") { task ->
      task.group = pluginName
      task.description = "Generate Laboratory feature factory."
      task.factory = factoryInput
      task.features = extension.factoryFeatureInputs
      task.codeGenDir = codeGenDir
      task.factoryClassName = "GeneratedFeatureFactory"
      task.factoryFunctionName = "featureGenerated"
      task.featureModelsMapper = { it }
    }
    addSourceSets(factoryTask, codeGenDir)
  }

  private fun Project.registerSourcedFeatureStorageTask() = afterEvaluate {
    val storageInput = extension.storageInput ?: return@afterEvaluate

    val codeGenDir = File("${project.buildDir}/generated/laboratory/code/sourced-storage")
    val storageTask = registerTask<SourcedFeatureStorageTask>("generateSourcedFeatureStorage") { task ->
      task.group = pluginName
      task.description = "Generate Laboratory sourced feature storage."
      task.storage = storageInput
      task.features = extension.factoryFeatureInputs
      task.codeGenDir = codeGenDir
    }
    addSourceSets(storageTask, codeGenDir)
  }

  private fun Project.registerFeatureSourcesFactoryTask() = afterEvaluate {
    val factoryInput = extension.featureSourcesFactory ?: return@afterEvaluate

    val codeGenDir = File("${project.buildDir}/generated/laboratory/code/feature-source-factory")
    val factoryTask = registerTask<FeatureFactoryTask>("generateFeatureSourceFactory") { task ->
      task.group = pluginName
      task.description = "Generate Laboratory feature sources factory."
      task.factory = factoryInput
      task.features = extension.factoryFeatureInputs
      task.codeGenDir = codeGenDir
      task.factoryClassName = "GeneratedFeatureSourceFactory"
      task.factoryFunctionName = "featureSourceGenerated"
      task.featureModelsMapper = { it.mapNotNull(FeatureFlagModel::source) }
    }
    addSourceSets(factoryTask, codeGenDir)
  }

  private fun Project.registerOptionFactoryTask() = afterEvaluate {
    val factoryInput = extension.optionFactoryInput ?: return@afterEvaluate

    val codeGenDir = File("${project.buildDir}/generated/laboratory/code/option-factory")
    val factoryTask = registerTask<OptionFactoryTask>("generateOptionFactory") { task ->
      task.group = pluginName
      task.description = "Generate Laboratory option factory."
      task.factory = factoryInput
      task.features = extension.factoryFeatureInputs
      task.codeGenDir = codeGenDir
    }
    addSourceSets(factoryTask, codeGenDir)
  }

  private fun Project.addLaboratoryDependency() {
    dependencies.add("api", "io.mehow.laboratory:laboratory:$laboratoryVersion")
  }

  private inline fun <reified T : Task> Project.registerTask(
    name: String,
    crossinline action: (T) -> Unit,
  ): TaskProvider<out T> {
    return tasks.register(name, T::class.java) { action(it) }
  }

  private fun Project.addSourceSets(task: TaskProvider<out Task>, dir: File) {
    if (hasAndroid.get()) {
      task.contributeToAndroidSourceSets(dir, this)
    } else {
      task.contributeToSourceSets(dir, this)
    }
  }
}
