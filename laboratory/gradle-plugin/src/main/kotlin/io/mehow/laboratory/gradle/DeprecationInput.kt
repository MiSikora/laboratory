package io.mehow.laboratory.gradle

import io.mehow.laboratory.generator.Deprecation
import java.io.Serializable
import kotlin.DeprecationLevel

internal class DeprecationInput(
  private val message: String,
  private val level: DeprecationLevel,
) : Serializable {
  fun toModel() = Deprecation(message, level)

  internal companion object {
    private const val serialVersionUID = 0L
  }
}
