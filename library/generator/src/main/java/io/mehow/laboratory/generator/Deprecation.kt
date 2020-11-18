package io.mehow.laboratory.generator

import kotlin.DeprecationLevel.WARNING

public data class Deprecation(
  val message: String,
  val level: DeprecationLevel = WARNING,
)
