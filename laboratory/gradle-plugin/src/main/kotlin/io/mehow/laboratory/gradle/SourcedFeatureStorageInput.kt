package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.SourcedFeatureStorageModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import java.io.Serializable

/**
 * Representation of a generated feature storage that is aware of feature flags sources.
 */
public class SourcedFeatureStorageInput internal constructor(
  packageNameProvider: PackageNameProvider,
) : Serializable {
  /**
   * Sets whether the generated feature storage should be public or internal.
   */
  public var isPublic: Boolean = false

  /**
   * Sets package name of the generated feature storage. Overwrites any previously set values.
   */
  public var packageName: String?
    get() = packageNameProvider.value
    set(value) = packageNameProvider.setValue(value)

  private val packageNameProvider = PackageNameProvider(packageNameProvider)

  internal fun toModel(sourceNames: List<String>) = SourcedFeatureStorageModel(
    visibility = if (isPublic) Public else Internal,
    className = ClassName(packageNameProvider.value.orEmpty(), "SourcedGeneratedFeatureStorage"),
    sourceNames = sourceNames,
  )

  internal companion object {
    private const val serialVersionUID = 0L
  }
}
