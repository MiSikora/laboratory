package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFactoryModel
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import java.io.Serializable

/**
 * Configuration for generating a feature factory or feature source factory.
 *
 * Feature factories aggregate feature flags or feature sources into a single generated utility
 * class.
 */
@LaboratoryDsl
public class FeatureFactoryInput internal constructor(packageNameProvider: PackageNameProvider) :
  Serializable {
  /** Controls whether the generated factory is public or internal. */
  public var isPublic: Boolean = false

  /** Overrides the package name for the generated factory. */
  public var packageName: String?
    get() = packageNameProvider.value
    set(value) = packageNameProvider.setValue(value)

  private val packageNameProvider = PackageNameProvider(packageNameProvider)

  internal fun toModel(features: List<FeatureFlagModel>, simpleName: String) =
    FeatureFactoryModel(
      visibility = if (isPublic) Public else Internal,
      className = ClassName(packageNameProvider.value.orEmpty(), simpleName),
      features = features,
    )

  internal companion object {
    private const val serialVersionUID = 1L
  }
}
