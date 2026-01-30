package io.mehow.laboratory

import io.mehow.laboratory.testing.AbstractStorageTest

class InMemoryStorageTest : AbstractStorageTest() {
  override fun storage() = Storage.inMemory()
}
