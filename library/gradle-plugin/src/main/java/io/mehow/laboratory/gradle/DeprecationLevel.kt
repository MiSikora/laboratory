package io.mehow.laboratory.gradle

import kotlin.DeprecationLevel as KotlinDeprecationLevel

/**
 * Possible levels of a deprecation. The level specifies how the deprecated element usages are reported in code.
 */
public enum class DeprecationLevel(
  internal val kotlinLevel: KotlinDeprecationLevel,
) {
  /** Usage of the deprecated element will be reported as a warning. */
  Warning(KotlinDeprecationLevel.WARNING),

  /** Usage of the deprecated element will be reported as an error. */
  Error(KotlinDeprecationLevel.ERROR),

  /** Deprecated element will not be accessible from code. */
  @Deprecated(
      message = "Does not work. See https://github.com/MiSikora/laboratory/issues/62.",
      level = KotlinDeprecationLevel.ERROR,
  )
  Hidden(KotlinDeprecationLevel.HIDDEN),
}
