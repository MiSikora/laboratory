package io.mehow.laboratory.compiler

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.mehow.laboratory.Feature
import java.io.File

internal class FeatureFlagGenerator(
  private val flag: FeatureFlagModel
) {
  private val typeSpec = TypeSpec.enumBuilder(flag.name)
    .addModifiers(flag.visibility.modifier)
    .addAnnotation(Feature::class)
    .let { builder -> flag.values.foldLeft(builder, TypeSpec.Builder::addEnumConstant) }
    .build()
  private val fileSpec = FileSpec.builder(flag.packageName, flag.name)
    .addType(typeSpec)
    .build()

  fun generate(output: File) = fileSpec.writeTo(output)
}
