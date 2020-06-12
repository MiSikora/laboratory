package io.mehow.laboratory.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.HasConvention
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KOTLIN_DSL_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

internal fun TaskProvider<out Task>.contributeToSourceSets(dir: File, project: Project) {
  makeKotlinDependOnTask(project)
  contribute(dir, project)
}

private fun TaskProvider<out Task>.makeKotlinDependOnTask(project: Project) {
  project.tasks.withType(KotlinCompile::class.java) { kotlinTask ->
    kotlinTask.dependsOn(this)
  }
}

private fun TaskProvider<out Task>.contribute(dir: File, project: Project) {
  if (contributeToAndroid(dir, project)) return
  contributeToKotlin(dir, project)
}

private fun TaskProvider<out Task>.contributeToAndroid(dir: File, project: Project): Boolean {
  val extension = project.extensions.findByType(BaseExtension::class.java) ?: return false
  val sources = extension.sourceSets.associate { set -> set.name to set.kotlin }
  for (variant in extension.variants) {
    val kotlinSourceSet = requireNotNull(sources[variant.name]) {
      "Did not find Kotlin source set for variant ${variant.name}"
    }
    kotlinSourceSet.srcDir(dir.toRelativeString(project.projectDir))
    variant.addJavaSourceFoldersToModel(dir)
    project.tasks.named("generate${variant.name.capitalize()}Sources").dependsOn(this)
  }
  return true
}

private fun contributeToKotlin(dir: File, project: Project) {
  val sourceSets = project.property("sourceSets") as SourceSetContainer
  val kotlinSourceSet = requireNotNull(sourceSets.getByName("main").kotlin) {
    "Did not find Kotlin source set"
  }
  kotlinSourceSet.srcDir(dir)
}

// Copied from SQLDelight with small modifications.
private val BaseExtension.variants: DomainObjectSet<out BaseVariant>
  get() = when (this) {
    is AppExtension -> applicationVariants
    is LibraryExtension -> libraryVariants
    else -> error("Unknown Android plugin $this")
  }

private val Any.kotlin: SourceDirectorySet?
  get() {
    val convention = getConvention(KOTLIN_DSL_NAME) ?: return null
    val sourceSetInterface = convention.javaClass
      .interfaces
      .find { it.name == KotlinSourceSet::class.qualifiedName }
    val getKotlin = sourceSetInterface?.methods?.find { it.name == "getKotlin" } ?: return null
    return getKotlin(convention) as? SourceDirectorySet
  }

private fun Any.getConvention(name: String) = (this as HasConvention).convention.plugins[name]
