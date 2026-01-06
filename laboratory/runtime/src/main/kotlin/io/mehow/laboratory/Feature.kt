package io.mehow.laboratory

/**
 * A feature flag with a single active option, selected through interaction with [Laboratory].
 *
 * Feature flags are defined as enum classes that implement this interface. Each enum constant
 * represents a possible option of the feature, and one of them must be marked as the default.
 *
 * Warning: Enum values must not override any methods. Serialization and option discovery rely on
 * stable class and enum constant names. Overriding methods on individual enum constants may break
 * this.
 */
public interface Feature<out T> where T : Feature<T>, T : Enum<out T> {
  /** Unique name of this feature option, used to identify it within its feature flag. */
  public val name: String

  /** The default option for this feature. This will be used if no other option is selected. */
  public val defaultOption: T

  /** The default source for resolving this feature's value. */
  public val defaultSource: Source
    get() = Source.Local

  /**
   * Optional description for this feature. Can be used to add context in debugging or QA tools.
   * Markdown-formatted links will be recognized and rendered as clickable hyperlinks in the QA UI.
   */
  public val description: String
    get() = ""

  /**
   * Represents a source of feature values.
   *
   * Features can have their active option resolved from either a local or remote source, depending
   * on configuration. The local source typically reflects user or app-provided overrides, while the
   * remote source may be populated from a backend or experimentation service.
   */
  public enum class Source(public val isLocal: Boolean) {
    Local(isLocal = true),
    Remote(isLocal = false);

    public val isRemote: Boolean
      get() = !isLocal
  }
}

/** All available options for this feature, as declared in the enum class. */
public val <T : Feature<T>> Class<T>.options: Array<T>
  get() = enumConstants

internal val <T : Feature<T>> Class<T>.firstOption: T
  get() = options.firstOrNull() ?: error("$canonicalName must have at least one option")
