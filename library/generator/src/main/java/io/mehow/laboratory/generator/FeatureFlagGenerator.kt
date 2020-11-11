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
  private val isDefaultValueOverride = ParameterSpec
      .builder(defaultValuePropertyName, Boolean::class, OVERRIDE)
      .defaultValue("%L", false)
      .build()

  private val primaryConstructor = FunSpec.constructorBuilder()
      .addParameter(isDefaultValueOverride)
      .build()

  private val isDefaultValueProperty = PropertySpec
      .builder(defaultValuePropertyName, Boolean::class)
      .initializer(defaultValuePropertyName)
      .build()

  private val suppressCast = AnnotationSpec.builder(Suppress::class)
      .addMember("%S", "UNCHECKED_CAST")
      .build()

  private val featureSource = feature.source?.let { nestedSource ->
    nestedSource to PropertySpec
        .builder(sourcePropertyName, featureType, OVERRIDE)
        .addAnnotation(suppressCast)
        .initializer("%T::class.java as %T", nestedSource.className, featureType)
        .build()
  }

  private val description = feature.description
      .takeIf { @Kt41142 it.isNotBlank() }
      ?.let { description ->
        PropertySpec
            .builder(descriptionPropertyName, String::class, OVERRIDE)
            .initializer("%S", description)
            .build()
      }

  private val typeSpec: TypeSpec = TypeSpec.enumBuilder(feature.className)
      .addModifiers(feature.visibility.modifier)
      .primaryConstructor(primaryConstructor)
      .addSuperinterface(Feature::class(feature.className))
      .addProperty(isDefaultValueProperty)
      .let { feature.values.foldLeft(it) { builder, featureValue -> builder.addEnumConstant(featureValue) } }
      .apply {
        featureSource?.let { (nestedSource, sourceWithOverride) ->
          addType(FeatureFlagGenerator(nestedSource).typeSpec)
          addProperty(sourceWithOverride)
        }
      }
      .apply { description?.let { @Kt41142 addProperty(it) } }
      .build()

  private fun TypeSpec.Builder.addEnumConstant(option: FeatureFlagOption) = if (option.isDefault) {
    val isDefaultValueArgument = CodeBlock.builder()
        .add("isDefaultValue = %L", true)
        .build()
    val overriddenConstructor = TypeSpec.anonymousClassBuilder()
        .addSuperclassConstructorParameter(isDefaultValueArgument)
        .build()
    addEnumConstant(option.name, overriddenConstructor)
  } else addEnumConstant(option.name)

  private val fileSpec = FileSpec.builder(feature.packageName, feature.name)
      .addType(typeSpec)
      .build()

  fun generate(output: File) = fileSpec.writeTo(output)

  private companion object {
    const val defaultValuePropertyName = "isDefaultValue"
    const val sourcePropertyName = "source"
    const val descriptionPropertyName = "description"

    val featureType = Class::class(Feature::class(STAR))

    operator fun KClass<*>.invoke(parameter: TypeName) = asClassName().parameterizedBy(parameter)
  }
}
