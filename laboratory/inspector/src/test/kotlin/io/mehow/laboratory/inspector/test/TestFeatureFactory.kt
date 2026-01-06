package io.mehow.laboratory.inspector.test

import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory

object TestFeatureFactory : FeatureFactory {
  @Suppress("UNCHECKED_CAST", "DEPRECATION", "DEPRECATION_ERROR")
  override fun create() =
    setOf(
      LocalFeature::class.java,
      RemoteFeature::class.java,
      DeprecatedWarningFeature::class.java,
      DeprecatedErrorFeature::class.java,
      FeatureWithDescription::class.java,
    )
      as Set<Class<Feature<*>>>
}

enum class LocalFeature : Feature<LocalFeature> {
  Value1,
  Value2;

  override val defaultOption
    get() = Value1

  override val defaultSource
    get() = Feature.Source.Local
}

enum class RemoteFeature : Feature<RemoteFeature> {
  Value3,
  Value4;

  override val defaultOption
    get() = Value3

  override val defaultSource
    get() = Feature.Source.Remote
}

enum class FeatureWithDescription : Feature<FeatureWithDescription> {
  Value5,
  Value6;

  override val defaultOption
    get() = Value5

  override val description: String
    get() = "Description with a [link](https://mehow.io/laboratory/)"
}

@Suppress("DEPRECATION")
@Deprecated(message = "", level = DeprecationLevel.WARNING)
enum class DeprecatedWarningFeature : Feature<DeprecatedWarningFeature> {
  Value7,
  Value8;

  override val defaultOption
    get() = Value7
}

@Suppress("DEPRECATION_ERROR")
@Deprecated(message = "", level = DeprecationLevel.ERROR)
enum class DeprecatedErrorFeature : Feature<DeprecatedErrorFeature> {
  Value9,
  Value10;

  override val defaultOption
    get() = Value9
}
