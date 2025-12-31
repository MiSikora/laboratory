package io.mehow.laboratory.datastore

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okio.Buffer
import okio.ByteString.Companion.decodeHex

internal class FeatureFlagsSerializerSpec :
  FunSpec({
    val flags = FeatureFlags(mapOf(FeatureA::class.java.toString() to FeatureA.A.name))
    val hex =
      "0a310a2c636c61737320696f2e6d65686f772e6c61626f7261746f72792e6461746173746f72652e4665617475726541120141"
    val binaryFlags = hex.decodeHex()

    test("decodes bytes") {
      val input = Buffer().write(binaryFlags).inputStream()

      val result = FeatureFlagsSerializer.readFrom(input)

      result shouldBe flags
    }

    test("encodes bytes") {
      val output = Buffer()

      FeatureFlagsSerializer.writeTo(flags, output.outputStream())
      val result = output.readByteString()

      result shouldBe binaryFlags
    }
  })
