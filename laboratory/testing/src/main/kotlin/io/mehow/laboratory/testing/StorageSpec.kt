package io.mehow.laboratory.testing

import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Storage
import kotlinx.coroutines.test.runTest
import org.junit.Test

abstract class AbstractStorageTest {
  abstract fun storage(): Storage

  @Test
  fun `read default string`() = runTest {
    val storage = storage()

    storage.getString("key").shouldBeNull()
  }

  @Test
  fun `store string`() = runTest {
    val storage = storage()

    storage.setString("key", "value")

    storage.getString("key") shouldBe "value"
  }

  @Test
  fun `store different string`() = runTest {
    val storage = storage()

    storage.setString("key1", "value1")
    storage.setString("key2", "value2")

    storage.getString("key1") shouldBe "value1"
    storage.getString("key2") shouldBe "value2"
  }

  @Test
  fun `update string`() = runTest {
    val storage = storage()

    storage.setString("key", "value1")
    storage.setString("key", "value2")

    storage.getString("key") shouldBe "value2"
  }

  @Test
  fun `store strings`() = runTest {
    val storage = storage()

    storage.setStrings(mapOf("key1" to "value1", "key2" to "value2"))

    storage.getString("key1") shouldBe "value1"
    storage.getString("key2") shouldBe "value2"
  }

  @Test
  fun `observe string`() = runTest {
    val storage = storage()

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

  @Test
  fun `read default boolean`() = runTest {
    val storage = storage()

    storage.getBoolean("key").shouldBeNull()
  }

  @Test
  fun `store boolean`() = runTest {
    val storage = storage()

    storage.setBoolean("key", true)

    storage.getBoolean("key") shouldBe true
  }

  @Test
  fun `store different boolean`() = runTest {
    val storage = storage()

    storage.setBoolean("key1", true)
    storage.setBoolean("key2", false)

    storage.getBoolean("key1") shouldBe true
    storage.getBoolean("key2") shouldBe false
  }

  @Test
  fun `update boolean`() = runTest {
    val storage = storage()

    storage.setBoolean("key", true)
    storage.setBoolean("key", false)

    storage.getBoolean("key") shouldBe false
  }

  @Test
  fun `store booleans`() = runTest {
    val storage = storage()

    storage.setBooleans(mapOf("key1" to true, "key2" to false))

    storage.getBoolean("key1") shouldBe true
    storage.getBoolean("key2") shouldBe false
  }

  @Test
  fun `observe boolean`() = runTest {
    val storage = storage()

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

  @Test
  fun `do not read boolean as string`() = runTest {
    val storage = storage()

    storage.setBoolean("key", true)

    storage.getString("key").shouldBeNull()
  }

  @Test
  fun `do not read string as boolean`() = runTest {
    val storage = storage()

    storage.setString("key", "value")

    storage.getBoolean("key").shouldBeNull()
  }

  @Test
  fun `save different value types separately for the same key`() = runTest {
    val storage = storage()

    storage.setString("key", "value")
    storage.setBoolean("key", true)

    storage.getString("key") shouldBe "value"
    storage.getBoolean("key") shouldBe true
  }

  @Test
  fun `clear data`() = runTest {
    val storage = storage()

    storage.setString("key1", "value")
    storage.setBoolean("key2", true)

    storage.clear()

    storage.getString("key1").shouldBeNull()
    storage.getBoolean("key2").shouldBeNull()
  }
}
