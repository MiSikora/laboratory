package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFactoryModel
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import java.io.Serializable

/** Representation of a generated feature factory class. */
public class FeatureFactoryInput internal constructor(packageNameProvider: PackageNameProvider) :
  Serializable {
  /** Sets whether the generated feature factory should be public or internal. */
  public var isPublic: Boolean = false

  /** Sets package name of the generated feature factory. Overwrites any previously set values. */
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
    private const val serialVersionUID = 0L
  }
}
