package io.mehow.laboratory.datastore

import androidx.datastore.Serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * [Serializer] that is capable of writing and reading of feature flags in an atomic way.
 */
object FeatureFlagsSerializer : Serializer<FeatureFlags> {
  override fun readFrom(input: InputStream): FeatureFlags {
    return FeatureFlags.ADAPTER.decode(input)
  }

  override fun writeTo(t: FeatureFlags, output: OutputStream) {
    FeatureFlags.ADAPTER.encode(output, t)
  }
}
