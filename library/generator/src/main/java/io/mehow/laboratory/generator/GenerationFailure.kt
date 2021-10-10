package io.mehow.laboratory.generator

public interface GenerationFailure {
  public val message: String

  public data class NoOption(
    private val fqcn: String,
  ) : GenerationFailure {
    override val message: String
      get() = "Feature flag must have at least one option. Found none in $fqcn."
  }

  public data class InvalidDefaultOption(
    private val fqcn: String,
    private val options: List<String>,
  ) : GenerationFailure {
    override val message: String
      get() = buildString {
        append("Feature flag must have a single default option. Found ")
        append(options.ifEmpty { "none" })
        append(" in ")
        append(fqcn)
      }
  }

  public data class MissingOption(
    private val fqcn: String,
    private val option: String,
  ) : GenerationFailure {
    override val message: String
      get() = "Feature flag $fqcn does not contain option $option."
  }

  public data class SelfSupervision(
    private val fqcn: String,
  ) : GenerationFailure {
    override val message: String
      get() = "Feature flag $fqcn cannot be supervisor of itself."
  }
}
