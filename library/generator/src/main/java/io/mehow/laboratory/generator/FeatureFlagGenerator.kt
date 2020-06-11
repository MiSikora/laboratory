package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.mehow.laboratory.Feature
import java.io.File

internal class FeatureFlagGenerator(
  private val feature: FeatureFlagModel
) {
  private val typeSpec = TypeSpec.enumBuilder(feature.name)
    .addModifiers(feature.visibility.modifier)
    .addAnnotation(Feature::class)
    .let { builder -> feature.values.foldLeft(builder, TypeSpec.Builder::addEnumConstant) }
    .build()
  private val fileSpec = FileSpec.builder(feature.packageName, feature.name)
    .addType(typeSpec)
    .build()

  fun generate(output: File) = fileSpec.writeTo(output)
}
