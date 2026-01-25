package io.mehow.laboratory.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import java.util.concurrent.atomic.AtomicBoolean
import org.gradle.api.Plugin
import org.gradle.api.Project

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
  private val hasKotlin = AtomicBoolean(false)
  private val hasAndroid = AtomicBoolean(false)
  private val isLaboratoryDependencyAdded = AtomicBoolean(false)

  override fun apply(target: Project) {
    val extension = target.extensions.create(PluginName, LaboratoryExtension::class.java)
    val tasks = LaboratoryTasks.registerIn(target)
    tasks.configureInput(target, extension)
    tasks.configureKotlinDependency(target)

    target.withPlugins(
      "org.jetbrains.kotlin.jvm",
      "org.jetbrains.kotlin.android",
      "com.android.experimental.built-in-kotlin",
    ) {
      hasKotlin.set(true)
    }

    target.withPlugins(
      "com.android.application",
      "com.android.library",
      "com.android.instantapp",
      "com.android.feature",
      "com.android.dynamic-feature",
    ) {
      hasAndroid.set(true)
      target.evaluateKotlinInAndroid()
      target.extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
        target.contributeToSources(tasks, ProjectSources.Android(variant))
      }
    }

    target.afterEvaluate {
      target.contributeToSources(tasks, ProjectSources.Kotlin(target))
      target.addLaboratoryDependency()
    }
  }

  private fun Project.evaluateKotlinInAndroid() {
    val isPropEnabled = findProperty("android.builtInKotlin")?.toString()?.toBoolean()
    extensions.getByType(AndroidComponentsExtension::class.java).finalizeDsl {
      val commonExt = extensions.getByType(CommonExtension::class.java)
      val isDslEnabled = commonExt.enableKotlin
      hasKotlin.set(isDslEnabled && isPropEnabled != false)
    }
  }

  private fun Project.contributeToSources(tasks: LaboratoryTasks, sources: ProjectSources) {
    val isKotlinContribution =
      when (sources) {
        is ProjectSources.Kotlin -> true
        is ProjectSources.Android -> false
      }
    if (hasAndroid.get() && isKotlinContribution) {
      return
    }
    check(hasKotlin.get()) { "Laboratory Gradle plugin applied in '$path' requires Kotlin plugin." }
    tasks.contributeToSources(sources)
  }

  private fun Project.addLaboratoryDependency() {
    dependencies.add("api", "io.mehow.laboratory:laboratory:$LibraryVersion")
  }
}

private fun Project.withPlugins(vararg ids: String, action: (Plugin<*>) -> Unit) {
  for (id in ids) {
    plugins.withId(id, action)
  }
}
