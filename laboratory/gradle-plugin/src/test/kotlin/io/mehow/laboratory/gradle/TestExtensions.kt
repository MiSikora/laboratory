package io.mehow.laboratory.gradle

import io.kotest.core.TestConfiguration
import java.io.File

internal fun String.toFixture() = File("src/test/projects/$this")

internal fun File.featureFile(
  fqcn: String,
  isAndroid: Boolean = false,
) = codeGenFile("feature-flags".expectedOutputDir(isAndroid), fqcn)

internal fun File.featureFactoryFile(
  fqcn: String,
  isAndroid: Boolean = false,
) = codeGenFile("feature-factory".expectedOutputDir(isAndroid), fqcn)

internal fun File.sourcedStorageFile(
  fqcn: String,
  isAndroid: Boolean = false,
) = codeGenFile("sourced-storage".expectedOutputDir(isAndroid), fqcn)

internal fun File.optionFactoryFile(
  fqcn: String,
  isAndroid: Boolean = false,
) = codeGenFile("option-factory".expectedOutputDir(isAndroid), fqcn)

internal fun File.featureSourceStorageFile(
  fqcn: String,
  isAndroid: Boolean = false,
) = codeGenFile("feature-source-factory".expectedOutputDir(isAndroid), fqcn)

private fun String.expectedOutputDir(isAndroid: Boolean) = if (isAndroid) {
  "java/" + when (this) {
    "feature-flags" -> "generateFeatureFlags"
    "feature-factory" -> "generateFeatureFactory"
    "sourced-storage" -> "generateSourcedFeatureStorage"
    "option-factory" -> "generateOptionFactory"
    "feature-source-factory" -> "generateFeatureSourceFactory"
    else -> error("Unknown output: $this")
  }
} else {
  "laboratory/code/$this"
}

private fun File.codeGenFile(
  dir: String,
  fqcn: String,
) = File(this, "build/generated/$dir/${fqcn.replace(".", "/")}.kt")

internal fun TestConfiguration.cleanBuildDirs() = beforeSpec {
  File("src/test/projects").getBuildDirs().forEach { it.deleteRecursively() }
}

private fun File.getBuildDirs(): List<File> = when (name) {
  "build" -> listOf(this)
  else -> listFiles().orEmpty().flatMap(File::getBuildDirs)
}
