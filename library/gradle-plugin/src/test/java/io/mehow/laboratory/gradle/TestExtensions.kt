package io.mehow.laboratory.gradle

import java.io.File

internal fun String.toFixture(): File {
  return File("src/test/projects/$this")
}

internal fun File.featureFile(fqcn: String): File {
  return File(this, "build/generated/source/laboratory/feature/${fqcn.replace(".", "/")}.kt")
}

internal fun File.factoryFile(fqcn: String): File {
  return File(this, "build/generated/source/laboratory/factory/${fqcn.replace(".", "/")}.kt")
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
