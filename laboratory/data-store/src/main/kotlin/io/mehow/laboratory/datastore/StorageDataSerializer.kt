package io.mehow.laboratory.datastore

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * [Serializer] that is capable of writing and reading of [DataStoreStorage] data. It can be used,
 * for example, as a delegate of encryption serializer.
 */
public object StorageDataSerializer : Serializer<StorageData> {
  override val defaultValue: StorageData = StorageData()

  override suspend fun readFrom(input: InputStream): StorageData {
    return StorageData.ADAPTER.decode(input)
  }

  override suspend fun writeTo(t: StorageData, output: OutputStream) {
    StorageData.ADAPTER.encode(output, t)
  }
}
