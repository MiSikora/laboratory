package io.mehow.laboratory.datastore

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * [Serializer] that is capable of writing and reading of feature flags. It can be used, for example,
 * as a delegate of encryption serializer.
 */
public object FeatureFlagsSerializer : Serializer<FeatureFlags> {
  override val defaultValue: FeatureFlags = FeatureFlags()

  override suspend fun readFrom(input: InputStream): FeatureFlags {
    return FeatureFlags.ADAPTER.decode(input)
  }

  override suspend fun writeTo(t: FeatureFlags, output: OutputStream) {
    FeatureFlags.ADAPTER.encode(output, t)
  }
}
