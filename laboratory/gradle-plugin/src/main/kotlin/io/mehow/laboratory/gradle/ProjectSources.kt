package io.mehow.laboratory.gradle

import com.android.build.api.variant.Variant
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

internal sealed interface ProjectSources {
  fun <T : OutputTask> srcDir(task: TaskProvider<out T>)

  class Kotlin(val project: Project) : ProjectSources {
    override fun <T : OutputTask> srcDir(task: TaskProvider<out T>) {
      val extension = project.extensions.getByType(KotlinSourceSetContainer::class.java)
      val sourceSets = extension.sourceSets.getByName("main").kotlin
      sourceSets.srcDir(task)
    }
  }

  class Android(val variant: Variant) : ProjectSources {
    override fun <T : OutputTask> srcDir(task: TaskProvider<out T>) {
      val sourceSets = variant.sources.kotlin!!
      sourceSets.addGeneratedSourceDirectory(task, OutputTask::outputDirectory)
    }
  }
}
