package io.mehow.laboratory

import io.kotest.core.spec.style.FunSpec
import io.mehow.laboratory.testing.addStorageSpec

class InMemoryStorageSpec : FunSpec() {
  init {
    val storage = Storage.inMemory()

    addStorageSpec(storage)
  }
}
