package io.mehow.laboratory.testing

import app.cash.turbine.test
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestScope
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Storage

fun FunSpec.addStorageSpec(storage: Storage) {
  beforeTest { test ->
    if (BaseSpec in test.config?.tags.orEmpty()) {
      storage.clear()
    }
  }

  baseTest("read default string") { storage.getString("key").shouldBeNull() }

  baseTest("store string") {
    storage.setString("key", "value")

    storage.getString("key") shouldBe "value"
  }

  baseTest("store different string") {
    storage.setString("key1", "value1")
    storage.setString("key2", "value2")

    storage.getString("key1") shouldBe "value1"
    storage.getString("key2") shouldBe "value2"
  }

  baseTest("update string") {
    storage.setString("key", "value1")
    storage.setString("key", "value2")

    storage.getString("key") shouldBe "value2"
  }

  baseTest("store strings") {
    storage.setStrings(mapOf("key1" to "value1", "key2" to "value2"))

    storage.getString("key1") shouldBe "value1"
    storage.getString("key2") shouldBe "value2"
  }

  baseTest("observe string") {
    storage.stringFlow("key1").test {
      awaitItem().shouldBeNull()

      storage.setString("key1", "value1")
      awaitItem() shouldBe "value1"

      storage.setString("key1", "value2")
      awaitItem() shouldBe "value2"

      storage.setString("key1", "value2")
      expectNoEvents()

      storage.setString("key2", "value1")
      expectNoEvents()

      storage.clear()
      awaitItem().shouldBeNull()
    }
  }

  baseTest("read default boolean") { storage.getBoolean("key").shouldBeNull() }

  baseTest("store boolean") {
    storage.setBoolean("key", true)

    storage.getBoolean("key") shouldBe true
  }

  baseTest("store different boolean") {
    storage.setBoolean("key1", true)
    storage.setBoolean("key2", false)

    storage.getBoolean("key1") shouldBe true
    storage.getBoolean("key2") shouldBe false
  }

  baseTest("update boolean") {
    storage.setBoolean("key", true)
    storage.setBoolean("key", false)

    storage.getBoolean("key") shouldBe false
  }

  baseTest("store booleans") {
    storage.setBooleans(mapOf("key1" to true, "key2" to false))

    storage.getBoolean("key1") shouldBe true
    storage.getBoolean("key2") shouldBe false
  }

  baseTest("observe boolean") {
    storage.booleanFlow("key1").test {
      awaitItem().shouldBeNull()

      storage.setBoolean("key1", true)
      awaitItem() shouldBe true

      storage.setBoolean("key1", false)
      awaitItem() shouldBe false

      storage.setBoolean("key1", false)
      expectNoEvents()

      storage.setBoolean("key2", true)
      expectNoEvents()

      storage.clear()
      awaitItem().shouldBeNull()
    }
  }

  baseTest("do not read boolean as string") {
    storage.setBoolean("key", true)

    storage.getString("key").shouldBeNull()
  }

  baseTest("do not read string as boolean") {
    storage.setString("key", "value")

    storage.getBoolean("key").shouldBeNull()
  }

  baseTest("save different value types separately for the same key") {
    storage.setString("key", "value")
    storage.setBoolean("key", true)

    storage.getString("key") shouldBe "value"
    storage.getBoolean("key") shouldBe true
  }

  baseTest("clear data") {
    storage.setString("key1", "value")
    storage.setBoolean("key2", true)

    storage.clear()

    storage.getString("key1").shouldBeNull()
    storage.getBoolean("key2").shouldBeNull()
  }
}

private fun FunSpec.baseTest(name: String, test: suspend TestScope.() -> Unit) {
  test(name).config(tags = setOf(BaseSpec), test = test)
}

private data object BaseSpec : Tag()
