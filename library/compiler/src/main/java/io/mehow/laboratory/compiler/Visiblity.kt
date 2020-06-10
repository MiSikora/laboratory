package io.mehow.laboratory.compiler

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.PUBLIC

enum class Visiblity(internal val modifier: KModifier) {
  Public(PUBLIC), Internal(INTERNAL)
}
