package io.mehow.laboratory.testing

import io.mehow.laboratory.Feature
import io.mehow.laboratory.options
import kotlin.reflect.KClass

val <T : Feature<T>> KClass<T>.options: List<T>
  get() = java.options.toList()

enum class FeatureA : Feature<FeatureA> {
  A,
  B,
  C;

  override val defaultOption
    get() = A
}

enum class FeatureB : Feature<FeatureB> {
  A,
  B,
  C;

  override val defaultOption
    get() = B
}

enum class FeatureC : Feature<FeatureC> {
  A,
  B,
  C;

  override val defaultOption
    get() = C
}

enum class RemoteFeatureA : Feature<RemoteFeatureA> {
  A,
  B,
  C;

  override val defaultOption
    get() = A

  override val defaultSource
    get() = Feature.Source.Remote
}

enum class RemoteFeatureB : Feature<RemoteFeatureB> {
  A,
  B,
  C;

  override val defaultOption
    get() = B

  override val defaultSource
    get() = Feature.Source.Remote
}

enum class RemoteFeatureC : Feature<RemoteFeatureC> {
  A,
  B,
  C;

  override val defaultOption
    get() = C

  override val defaultSource
    get() = Feature.Source.Remote
}

enum class FeatureWithDescription : Feature<FeatureWithDescription> {
  A;

  override val defaultOption
    get() = A

  override val description: String
    get() = "Description with a [hyperlink](https://mehow.io/laboratory)"
}

enum class FeatureWithoutDescription : Feature<FeatureWithoutDescription> {
  A;

  override val defaultOption
    get() = A
}

enum class FeatureWithoutValues : Feature<FeatureWithoutValues>
