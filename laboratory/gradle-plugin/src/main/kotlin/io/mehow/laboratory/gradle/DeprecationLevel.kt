package io.mehow.laboratory.gradle

import kotlin.DeprecationLevel as KotlinDeprecationLevel

/** Defines how strictly deprecated feature flags are treated by the compiler. */
public enum class DeprecationLevel(internal val kotlinLevel: KotlinDeprecationLevel) {
  /** Using the feature produces a compile-time warning. */
  Warning(KotlinDeprecationLevel.WARNING),

  /** Using the feature produces a compile-time error. */
  Error(KotlinDeprecationLevel.ERROR),

  /** The feature is hidden and cannot be referenced from the source code. */
  Hidden(KotlinDeprecationLevel.HIDDEN),
}
