package io.mehow.laboratory.gradle

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.FeatureFlagModel
import io.mehow.laboratory.generator.OptionFactoryModel
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import org.gradle.api.Project
import java.util.function.Predicate

/**
 * Representation of a generated option factory that is aware of feature flags.
 */
public class OptionFactoryInput internal constructor(
  private val packageNameProvider: () -> String,
) {
  /**
   * Sets whether the generated option factory should be public or internal.
   */
  public var isPublic: Boolean = false

  /**
   * Sets package name of the generated option factory. Overwrites any previously set values.
   */
  public var packageName: String? = null

  internal var projectFilter = Predicate<Project> { true }
    private set

  /**
   * Sets which Gradle projects should be included to contributing
   * their feature flags to the generated option factory.
   */
  public fun projectFilter(filter: Predicate<Project>) {
    projectFilter = filter
  }

  internal fun toBuilder(features: List<FeatureFlagModel>) = OptionFactoryModel.Builder(
      visibility = if (isPublic) Public else Internal,
      className = ClassName(packageName ?: packageNameProvider(), "GeneratedOptionFactory"),
      features = features,
  )
}
