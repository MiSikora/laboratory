package io.mehow.laboratory.gradle

import io.kotest.core.TestConfiguration
import java.io.File

internal fun String.toFixture() = File("src/test/projects/$this")

internal fun File.featureFile(fqcn: String) = codeGenFile("feature-flags", fqcn)

internal fun File.featureFactoryFile(fqcn: String) = codeGenFile("feature-factory", fqcn)

internal fun File.sourcedStorageFile(fqcn: String) = codeGenFile("sourced-storage", fqcn)

internal fun File.featureSourceStorageFile(fqcn: String) = codeGenFile("feature-source-factory", fqcn)

internal fun File.optionFactoryFile(fqcn: String) = codeGenFile("option-factory", fqcn)

private fun File.codeGenFile(dir: String, fqcn: String) = File(
    this,
    "build/generated/laboratory/code/$dir/${fqcn.replace(".", "/")}.kt"
)

internal fun TestConfiguration.cleanBuildDirs() = beforeSpec {
  File("src/test/projects").getBuildDirs().forEach { it.deleteRecursively() }
}

private fun File.getBuildDirs(): List<File> = when (name) {
  "build" -> listOf(this)
  else -> listFiles().orEmpty().flatMap(File::getBuildDirs)
}
