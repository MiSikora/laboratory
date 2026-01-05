package io.mehow.laboratory.gradle

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

@DslMarker public annotation class LaboratoryDsl

/**
 * Gradle extension used to configure feature flags and code generation.
 *
 * This extension is available as `laboratory { ... }` in the build script. It allows defining
 * feature flags and selecting which factories and storage helpers should be generated for the
 * module.
 */
@LaboratoryDsl
public abstract class LaboratoryExtension
@Inject
internal constructor(private val project: Project) {
  /**
   * Base package name for all generated code.
   *
   * Individual features or generated factories may override this value.
   */
  public var packageName: String
    get() = packageNameProvider.value.orEmpty()
    set(value) {
      packageNameProvider.setValue(value)
    }

  private val packageNameProvider = PackageNameProvider()

  private val _featureInputs = mutableListOf<FeatureFlagInput>()

  internal val featureInputs
    get() = _featureInputs.toList()

  private val externalDependencies = mutableMapOf<DependencyContribution, List<FeatureFlagInput>>()

  internal val factoryFeatureInputs
    get() = buildMap {
      DependencyContribution.entries.forEach { entry ->
        put(entry, featureInputs + externalDependencies[entry].orEmpty())
      }
    }

  /**
   * Defines a feature flag with multiple selectable options.
   *
   * Use this for features that can have more than two possible values. At least one option must be
   * defined, and exactly one option must be marked as the default.
   */
  public fun feature(name: String, action: Action<FeatureFlagInput.MultiOption>) {
    _featureInputs += FeatureFlagInput.MultiOption(name, packageNameProvider).apply(action::execute)
  }

  /**
   * Defines a binary feature flag that is enabled by default.
   *
   * The generated feature has two states: `Enabled` and `Disabled`. `Enabled` is treated as the
   * default value.
   */
  @JvmOverloads
  public fun enabledFeature(
    name: String,
    action: Action<FeatureFlagInput.BinaryOption> = Action {},
  ) {
    binaryFeature(name, isEnabled = true, action)
  }

  /**
   * Defines a binary feature flag that is disabled by default.
   *
   * The generated feature has two states: `Enabled` and `Disabled`. `Disabled` is treated as the
   * default value.
   */
  @JvmOverloads
  public fun disabledFeature(
    name: String,
    action: Action<FeatureFlagInput.BinaryOption> = Action {},
  ) {
    binaryFeature(name, isEnabled = false, action)
  }

  private fun binaryFeature(
    name: String,
    isEnabled: Boolean,
    action: Action<FeatureFlagInput.BinaryOption>,
  ) {
    _featureInputs +=
      FeatureFlagInput.BinaryOption(name, isEnabled, packageNameProvider).apply(action::execute)
  }

  internal var factoryInput: FeatureFactoryInput? = null
    private set

  /**
   * Generates a feature factory for this module.
   *
   * A feature factory aggregates all feature flags into a single utility, commonly used in
   * top-level or QA modules.
   */
  @JvmOverloads
  public fun featureFactory(action: Action<FeatureFactoryInput> = Action {}) {
    factoryInput = FeatureFactoryInput(packageNameProvider).apply(action::execute)
  }

  internal var optionFactoryInput: OptionFactoryInput? = null
    private set

  /**
   * Generates an option factory for creating feature flags from string keys.
   *
   * This is commonly used when integrating with remote configuration systems.
   */
  @JvmOverloads
  public fun optionFactory(action: Action<OptionFactoryInput> = Action {}) {
    optionFactoryInput = OptionFactoryInput(packageNameProvider).apply(action::execute)
  }

  /**
   * Includes feature flags from another Gradle project in this module’s generated outputs.
   *
   * The dependent project must also apply the Laboratory plugin.
   */
  @JvmOverloads
  public fun dependency(
    project: ProjectDependency,
    contributeTo: Collection<DependencyContribution> = DependencyContribution.entries,
  ) {
    dependency(this.project.project(project.path), contributeTo)
  }

  /**
   * Includes feature flags from another Gradle project in this module’s generated outputs.
   *
   * The dependent project must also apply the Laboratory plugin.
   */
  @JvmOverloads
  public fun dependency(
    project: Project,
    contributeTo: Collection<DependencyContribution> = DependencyContribution.entries,
  ) {
    require(contributeTo.isNotEmpty()) {
      "Dependency in project '${this.project.name}' on '${project.name}' must have at least one contribution"
    }
    this.project.evaluationDependsOn(project.path)
    val laboratoryExtension =
      requireNotNull(project.extensions.findByType(LaboratoryExtension::class.java)) {
        "Cannot depend on a project without laboratory plugin"
      }
    contributeTo.forEach { entry ->
      externalDependencies[entry] =
        externalDependencies[entry].orEmpty() + laboratoryExtension.featureInputs
    }
  }
}
