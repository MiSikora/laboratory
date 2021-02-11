package io.mehow.laboratory

/**
 * A feature flag that has one active option. Options are selected by interaction of this interface with [Laboratory].
 * Feature flag is a enum that implements this interface.
 *
 * **Warning**: Enum values cannot individually override any functions. Otherwise serialization, and consequentially
 * discovery of a selection option, will not work as it is based on a class name.
 */
public interface Feature<T> : Comparable<T> where T : Feature<T>, T : Enum<T> {
  /**
   * A name of an option that should uniquely identify it within this feature flag.
   */
  public val name: String

  /**
   * Determines which option is a default for this feature flag.
   */
  public val defaultOption: T

  /**
   * Source of feature flag values. When `null` it is assumed that the feature flag has only local source of values.
   * Source is also a feature flag, which means it can be controlled by [Laboratory] as well. By convention,
   * if this property is not `null`, one of the source values should be `Local`. For example, the following code
   * will result in a flag that can be controlled by local, Firebase or Aws source.
   *
   * ```
   * enum class SomeFeature : Feature<SomeFeature> {
   *   FirstValue,
   *   SecondValue,
   *   ;
   *
   *   override val defaultOption get() = FirstValue
   *
   *   @Suppress("UNCHECKED_CAST")
   *   override val source = Source::class.java as Class<Feature<*>>
   *
   *   enum class Source : Feature<Source> {
   *     Local,
   *     Firebase,
   *     Aws,
   *     ;
   *
   *     override val defaultOption get() = Local
   *   }
   * }
   * ```
   */
  @JvmDefault public val source: Class<Feature<*>>? get() = null

  /**
   * Description of the feature flag that can be used for more contextual information. Markdown formatted links
   * will be picked up by the QA module and represented as hyperlinks.
   */
  @JvmDefault public val description: String get() = ""

  /**
   * Option of another feature flag that controls value of this child flag. When parent feature flag
   * has an option different from this value then the child does not produce values other than the default one.
   * Option can still be set via `Laboratory` but it will not be exposed as long as a feature flag is not supervised.
   */
  @JvmDefault public val supervisorOption: Feature<*>? get() = null
}

/**
 * Default option of a feature flag.
 *
 * @see Feature.defaultOption
 */
public val <T : Feature<T>> Class<T>.defaultOption: T
  get() = firstOption.defaultOption

/**
 * Source of feature flag values.
 *
 * @see Feature.source
 */
public val Class<Feature<*>>.source: Class<Feature<*>>?
  get() = firstOption.source

/**
 * Description of a feature flag.
 *
 * @see Feature.description
 */
public val Class<Feature<*>>.description: String
  get() = firstOption.description

/**
 * Parent of a feature flag.
 *
 * @see Feature.supervisorOption
 */
public val <T : Feature<T>> Class<T>.parent: Feature<*>?
  get() = firstOption.supervisorOption

/**
 * All available options of a feature flag.
 */
public val <T : Feature<T>> Class<T>.options: Array<T> get() = enumConstants

internal val <T : Feature<T>> Class<T>.firstOption
  get() = options.firstOrNull() ?: error("$canonicalName must have at least one option")
