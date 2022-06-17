package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFactoryModel
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

/**
 * Representation of a generated feature factory class.
 */
public class FeatureFactoryInput internal constructor(
  private val parentPackageName: String,
) {
  /**
   * Sets whether the generated feature factory should be public or internal.
   */
  public var isPublic: Boolean = false

  /**
   * Sets package name of the generated feature factory. Overwrites any previously set values.
   */
  public var packageName: String? = null

  internal fun toModel(features: List<FeatureFlagModel>, simpleName: String) = FeatureFactoryModel(
      visibility = if (isPublic) Public else Internal,
      className = ClassName(packageName ?: parentPackageName, simpleName),
      features = features,
  )
}
