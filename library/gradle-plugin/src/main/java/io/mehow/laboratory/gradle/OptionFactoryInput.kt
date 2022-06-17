package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.OptionFactoryModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public

/**
 * Representation of a generated option factory that is aware of feature flags.
 */
public class OptionFactoryInput internal constructor(
  private val parentPackageName: String,
) {
  /**
   * Sets whether the generated option factory should be public or internal.
   */
  public var isPublic: Boolean = false

  /**
   * Sets package name of the generated option factory. Overwrites any previously set values.
   */
  public var packageName: String? = null

  internal fun toModel(features: List<FeatureFlagModel>) = OptionFactoryModel(
      visibility = if (isPublic) Public else Internal,
      className = ClassName(packageName ?: parentPackageName, "GeneratedOptionFactory"),
      features = features,
  )
}
