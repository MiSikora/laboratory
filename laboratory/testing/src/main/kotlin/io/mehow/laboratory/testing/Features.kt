package io.mehow.laboratory.testing

import io.mehow.laboratory.BinaryFeature
import io.mehow.laboratory.Feature
import io.mehow.laboratory.internal.InternalLaboratoryApi
import io.mehow.laboratory.internal.options
import kotlin.reflect.KClass

@OptIn(InternalLaboratoryApi::class)
val <T> KClass<T>.options: List<T> where T : Feature<T>, T : Enum<T>
  get() = java.options

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

enum class EnabledBinaryFeature(override val binaryValue: Boolean) :
  BinaryFeature<EnabledBinaryFeature> {
  Enabled(binaryValue = true),
  Disabled(binaryValue = false);

  override val defaultOption
    get() = Enabled
}

enum class DisabledBinaryFeature(override val binaryValue: Boolean) :
  BinaryFeature<DisabledBinaryFeature> {
  Enabled(binaryValue = true),
  Disabled(binaryValue = false);

  override val defaultOption
    get() = Disabled
}
