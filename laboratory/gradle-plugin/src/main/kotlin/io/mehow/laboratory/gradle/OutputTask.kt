package io.mehow.laboratory.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty

internal abstract class OutputTask : DefaultTask() {
  abstract val outputDirectory: DirectoryProperty
}
