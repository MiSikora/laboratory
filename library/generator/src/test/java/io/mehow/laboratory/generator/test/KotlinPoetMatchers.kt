package io.mehow.laboratory.generator.test

import com.squareup.kotlinpoet.FileSpec
import io.kotest.matchers.shouldBe

internal infix fun FileSpec.shouldSpecify(value: String) = toString() shouldBe value
