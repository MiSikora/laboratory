package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
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
  private val defaultOptionProperty = feature.options.toList()
      .single { @Kt41142 it.isDefault }
      .let { option ->
        PropertySpec
            .builder(defaultOptionPropertyName, feature.className, OVERRIDE)
            .getter(FunSpec.getterBuilder().addCode("return %L", option.name).build())
            .build()
      }

  private val suppressCast = AnnotationSpec.builder(Suppress::class)
      .addMember("%S", "UNCHECKED_CAST")
      .build()

  private val sourceProperty = feature.source?.let { nestedSource ->
    nestedSource to PropertySpec
        .builder(sourcePropertyName, featureType, OVERRIDE)
        .addAnnotation(suppressCast)
        .initializer("%T::class.java as %T", nestedSource.className, featureType)
        .build()
  }

  private val descriptionProperty = feature.description
      .takeIf { @Kt41142 it.isNotBlank() }
      ?.let { description ->
        PropertySpec
            .builder(descriptionPropertyName, String::class, OVERRIDE)
            .initializer("%S", description)
            .build()
      }

  private val typeSpec: TypeSpec = TypeSpec.enumBuilder(feature.className)
      .addModifiers(feature.visibility.modifier)
      .addSuperinterface(Feature::class(feature.className))
      .addProperty(defaultOptionProperty)
      .let { feature.options.foldLeft(it) { builder, featureOption -> builder.addEnumConstant(featureOption.name) } }
      .apply {
        sourceProperty?.let { (nestedSource, sourceWithOverride) ->
          addType(FeatureFlagGenerator(nestedSource).typeSpec)
          addProperty(sourceWithOverride)
        }
      }
      .apply { descriptionProperty?.let { @Kt41142 addProperty(it) } }
      .build()

  private val fileSpec = FileSpec.builder(feature.packageName, feature.name)
      .addType(typeSpec)
      .build()

  fun generate(output: File) = fileSpec.writeTo(output)

  private companion object {
    const val defaultOptionPropertyName = "defaultOption"
    const val sourcePropertyName = "source"
    const val descriptionPropertyName = "description"

    val featureType = Class::class(Feature::class(STAR))

    operator fun KClass<*>.invoke(parameter: TypeName) = asClassName().parameterizedBy(parameter)
  }
}
