package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.PUBLIC

enum class Visibility(internal val modifier: KModifier) {
  Public(PUBLIC),
  Internal(INTERNAL)
}
