package io.mehow.laboratory.gradle

import java.io.File

internal fun String.toFixture(): File {
  return File("src/test/projects/$this")
}

internal fun File.featureFile(fqcn: String): File {
  return File(this, "build/generated/laboratory/code/feature-flags/${fqcn.replace(".", "/")}.kt")
}

internal fun File.featureFactoryFile(fqcn: String): File {
  return File(this, "build/generated/laboratory/code/feature-factory/${fqcn.replace(".", "/")}.kt")
}

internal fun File.sourcedStorageFile(fqcn: String): File {
  return File(this, "build/generated/laboratory/code/sourced-storage/${fqcn.replace(".", "/")}.kt")
}

internal fun File.featureSourceStorageFile(fqcn: String): File {
  return File(this, "build/generated/laboratory/code/feature-source-factory/${fqcn.replace(".", "/")}.kt")
}

internal fun File.cleanUpDir() {
  if (isDirectory) {
    for (file in listFiles()!!) file.cleanUpDir()
  }
  delete()
}

internal fun File.getOutputDirs(): List<File> = when {
  !isDirectory -> emptyList()
  name == "build" -> listOf(this)
  else -> listFiles()!!.flatMap(File::getOutputDirs)
}
