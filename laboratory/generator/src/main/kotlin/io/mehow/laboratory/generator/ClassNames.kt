package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

internal operator fun KClass<*>.invoke(
  parameter: TypeName,
  vararg parameters: TypeName,
) = asClassName().parameterizedBy(parameter, *parameters)
