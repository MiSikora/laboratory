package io.mehow.laboratory.gradle

import io.mehow.laboratory.laboratoryVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

private const val pluginName = "laboratory"

class LaboratoryPlugin : Plugin<Project> {
  private val hasKotlin = AtomicBoolean(false)
  private val hasAndroid = AtomicBoolean(false)
  private lateinit var extension: LaboratoryExtension

  override fun apply(project: Project) {
    extension = project.extensions.create(pluginName, LaboratoryExtension::class.java)
    project.requireKotlinPlugin()
    project.checkIfHasAndroid()

    project.registerFeaturesTask()
    project.registerFactoryTask()
  }

  private fun Project.requireKotlinPlugin() {
    val kotlinPluginHandler = { _: Plugin<*> -> hasKotlin.set(true) }
    plugins.withId("org.jetbrains.kotlin.android", kotlinPluginHandler)
    plugins.withId("org.jetbrains.kotlin.jvm", kotlinPluginHandler)

    afterEvaluate {
      check(hasKotlin.get()) { "Laboratory Gradle plugin requires Kotlin plugin." }
      addLaboratoryDependency()
    }
  }

  private fun Project.checkIfHasAndroid() {
    val androidPluginHandler = { _: Plugin<*> -> hasAndroid.set(true) }
    plugins.withId("com.android.application", androidPluginHandler)
    plugins.withId("com.android.library", androidPluginHandler)
    plugins.withId("com.android.instantapp", androidPluginHandler)
    plugins.withId("com.android.feature", androidPluginHandler)
    plugins.withId("com.android.dynamic-feature", androidPluginHandler)
  }

  private fun Project.registerFeaturesTask() = afterEvaluate {
    val codeGenDir = File("$buildDir/generated/laboratory/code/feature")
    val featuresTask = registerTask<FeatureFlagsTask>("generateFeatureFlags") { task ->
      task.group = pluginName
      task.description = "Generate Laboratory features."
      task.features = extension.featureInputs
      task.codeGenDir = codeGenDir
    }
    featuresTask.contributeToSourceSets(codeGenDir, this)
  }

  private fun Project.registerFactoryTask() = afterEvaluate {
    val factoryInput = extension.factoryInput ?: return@afterEvaluate

    val codeGenDir = File("${project.buildDir}/generated/laboratory/code/factory")
    val featureInputs = mutableListOf<FeatureFlagInput>()
    val factoryTask = registerTask<FeatureFactoryTask>("generateFeatureFactory") { task ->
      task.group = pluginName
      task.description = "Generate Laboratory feature factory."
      task.factory = factoryInput
      task.features = featureInputs
      task.codeGenDir = codeGenDir
    }
    findAllFeatures(factoryInput.projectFilter) { featureInputs.addAll(it) }
    factoryTask.contributeToSourceSets(codeGenDir, this)
  }

  private fun Project.findAllFeatures(
    projectFilter: ProjectFilter,
    onFeatureInputsFound: (List<FeatureFlagInput>) -> Unit,
  ) {
    rootProject.allprojects { project ->
      if (!projectFilter.reject(project)) {
        project.plugins.withType(LaboratoryPlugin::class.java) { labPlugin ->
          val pluginFeatures = labPlugin.extension.featureInputs
          onFeatureInputsFound(pluginFeatures)
          if (pluginFeatures.isEmpty()) {
            project.afterEvaluate { onFeatureInputsFound(labPlugin.extension.featureInputs) }
          }
        }
      }
    }
  }

  private fun Project.addLaboratoryDependency() {
    val artifactId = if (hasAndroid.get()) "shared-preferences" else "laboratory"
    val dependency = "io.mehow.laboratory:$artifactId:$laboratoryVersion"
    dependencies.add("api", dependency)
  }

  private inline fun <reified T : Task> Project.registerTask(
    name: String,
    crossinline action: (T) -> Unit,
  ): TaskProvider<out T> {
    return tasks.register(name, T::class.java) { action(it) }
  }
}
