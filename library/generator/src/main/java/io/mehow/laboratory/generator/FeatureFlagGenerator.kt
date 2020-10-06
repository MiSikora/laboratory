package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import io.mehow.laboratory.Feature
import java.io.File
import kotlin.reflect.KClass

internal class FeatureFlagGenerator(
  private val feature: FeatureFlagModel,
) {
  private val isFallbackValueOverride = ParameterSpec
    .builder(fallbackValuePropertyName, Boolean::class, OVERRIDE)
    .defaultValue("%L", false)
    .build()

  private val primaryConstructor = FunSpec.constructorBuilder()
    .addParameter(isFallbackValueOverride)
    .build()

  private val isFallbackValueProperty = PropertySpec
    .builder(fallbackValuePropertyName, Boolean::class)
    .initializer(fallbackValuePropertyName)
    .build()

  private val suppressCast = AnnotationSpec.builder(Suppress::class)
    .addMember("%S", "UNCHECKED_CAST")
    .build()

  private val featureSource = feature.nestedSource?.let { nestedSource ->
    nestedSource to PropertySpec
      .builder(sourcedWithPropertyName, featureType, OVERRIDE)
      .addAnnotation(suppressCast)
      .initializer("%T::class.java as %T", nestedSource.className, featureType)
      .build()
  }

  private val typeSpec: TypeSpec = TypeSpec.enumBuilder(feature.className)
    .addModifiers(feature.visibility.modifier)
    .primaryConstructor(primaryConstructor)
    .addSuperinterface(Feature::class(feature.className))
    .addProperty(isFallbackValueProperty)
    .let { feature.values.foldLeft(it) { builder, featureValue -> builder.addEnumConstant(featureValue) } }
    .apply {
      featureSource?.let { (nestedSource, sourceWithOverride) ->
        addType(FeatureFlagGenerator(nestedSource).typeSpec)
        addProperty(sourceWithOverride)
      }
    }
    .build()

  private fun TypeSpec.Builder.addEnumConstant(featureValue: FeatureValue) = if (featureValue.isFallbackValue) {
    val isFallbackValueArgument = CodeBlock.builder()
      .add("isFallbackValue = %L", true)
      .build()
    val overriddenConstructor = TypeSpec.anonymousClassBuilder()
      .addSuperclassConstructorParameter(isFallbackValueArgument)
      .build()
    addEnumConstant(featureValue.name, overriddenConstructor)
  } else addEnumConstant(featureValue.name)

  private val fileSpec = FileSpec.builder(feature.packageName, feature.name)
    .addType(typeSpec)
    .build()

  fun generate(output: File) = fileSpec.writeTo(output)

  private companion object {
    const val fallbackValuePropertyName = "isFallbackValue"
    const val sourcedWithPropertyName = "sourcedWith"

    val featureType = Class::class(Feature::class(STAR))

    operator fun KClass<*>.invoke(parameter: TypeName) = asClassName().parameterizedBy(parameter)
  }
}
