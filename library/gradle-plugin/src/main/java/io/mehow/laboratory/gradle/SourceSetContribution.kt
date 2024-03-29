package io.mehow.laboratory.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.util.Locale

internal fun TaskProvider<out Task>.contributeToSourceSets(dir: File, project: Project) {
  makeKotlinDependOnTask(project)
  contributeToKotlin(dir, project)
}

internal fun TaskProvider<out Task>.contributeToAndroidSourceSets(dir: File, project: Project) {
  makeKotlinDependOnTask(project)
  contributeToAndroid(dir, project)
}

private fun TaskProvider<out Task>.makeKotlinDependOnTask(project: Project) {
  project.tasks.withType(KotlinCompile::class.java).configureEach { kotlinTask ->
    kotlinTask.dependsOn(this)
  }
}

private fun contributeToKotlin(dir: File, project: Project) {
  val sourceSets = project.extensions.getByType(KotlinProjectExtension::class.java).sourceSets
  val kotlinSourceSet = sourceSets.getByName("main").kotlin
  kotlinSourceSet.srcDir(dir)
}

private fun TaskProvider<out Task>.contributeToAndroid(dir: File, project: Project) {
  val extension = requireNotNull(project.extensions.findByType(BaseExtension::class.java)) {
    "Did not find BaseExtension in Android project"
  }
  val kotlinSourceSets = project.extensions.getByType(KotlinProjectExtension::class.java).sourceSets
  val sources = extension.sourceSets.associate { set ->
    set.name to kotlinSourceSets.getByName(set.name).kotlin
  }
  for (variant in extension.variants) {
    val kotlinSourceSet = requireNotNull(sources[variant.name]) {
      "Did not find Kotlin source set for variant ${variant.name}"
    }
    kotlinSourceSet.srcDir(dir.toRelativeString(project.projectDir))
    variant.addJavaSourceFoldersToModel(dir)

    fun String.titleCaseFirstChar() = replaceFirstChar {
      if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
    }

    project.tasks.named("generate${variant.name.titleCaseFirstChar()}Sources").configure {
      it.dependsOn(this)
    }
  }
}

// Copied from SQLDelight with small modifications.
private val BaseExtension.variants: DomainObjectSet<out BaseVariant>
  get() = when (this) {
    is AppExtension -> applicationVariants
    is LibraryExtension -> libraryVariants
    else -> throw GradleException("Unknown Android plugin $this")
  }
