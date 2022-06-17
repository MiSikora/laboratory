package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.SourcedFeatureStorageModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

/**
 * Representation of a generated feature storage that is aware of feature flags sources.
 */
public class SourcedFeatureStorageInput internal constructor(
  private val parentPackageName: String,
) {
  /**
   * Sets whether the generated feature storage should be public or internal.
   */
  public var isPublic: Boolean = false

  /**
   * Sets package name of the generated feature storage. Overwrites any previously set values.
   */
  public var packageName: String? = null

  internal fun toModel(sourceNames: List<String>) = SourcedFeatureStorageModel(
      visibility = if (isPublic) Public else Internal,
      className = ClassName(packageName ?: parentPackageName, "SourcedGeneratedFeatureStorage"),
      sourceNames = sourceNames,
  )
}
