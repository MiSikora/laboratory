package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFactoryModel
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import org.gradle.api.Project
import java.util.function.Predicate

/**
 * Representation of a generated feature factory class.
 */
public class FeatureFactoryInput internal constructor(
  private val packageNameProvider: () -> String,
) {
  /**
   * Sets whether the generated feature factory should be public or internal.
   */
  public var isPublic: Boolean = false

  /**
   * Sets package name of the generated feature factory. Overwrites any previously set values.
   */
  public var packageName: String? = null

  internal var projectFilter = Predicate<Project> { true }
    private set

  /**
   * Sets which Gradle projects should be included to contributing
   * their feature flags to the generated feature factory.
   */
  public fun projectFilter(filter: Predicate<Project>) {
    projectFilter = filter
  }

  internal fun toBuilder(features: List<FeatureFlagModel>, simpleName: String) = FeatureFactoryModel.Builder(
      visibility = if (isPublic) Public else Internal,
      className = ClassName(packageName ?: packageNameProvider(), simpleName),
      features = features,
  )
}
