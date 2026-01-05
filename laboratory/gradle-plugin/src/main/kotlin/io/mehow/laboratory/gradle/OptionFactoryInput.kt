package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.OptionFactoryModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import java.io.Serializable

/**
 * Configuration for generating an option factory.
 *
 * Option factories create feature flag instances from string-based keys and option names.
 */
@LaboratoryDsl
public class OptionFactoryInput internal constructor(packageNameProvider: PackageNameProvider) :
  Serializable {
  /** Controls whether the generated option factory is public or internal. */
  public var isPublic: Boolean = false

  /** Overrides the package name for the generated option factory. */
  public var packageName: String?
    get() = packageNameProvider.value
    set(value) = packageNameProvider.setValue(value)

  private val packageNameProvider = PackageNameProvider(packageNameProvider)

  internal fun toModel(features: List<FeatureFlagModel>) =
    OptionFactoryModel(
      visibility = if (isPublic) Public else Internal,
      className = ClassName(packageNameProvider.value.orEmpty(), "GeneratedOptionFactory"),
      features = features,
    )

  internal companion object {
    private const val serialVersionUID = 1L
  }
}
