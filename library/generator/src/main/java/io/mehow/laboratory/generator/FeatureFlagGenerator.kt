package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
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
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.HIDDEN
import kotlin.DeprecationLevel.WARNING
import kotlin.reflect.KClass

@Suppress("StringLiteralDuplication")
internal class FeatureFlagGenerator(
  private val feature: FeatureFlagModel,
) {
  private val deprecated = feature.deprecation?.let { deprecation ->
    AnnotationSpec.builder(Deprecated::class)
        .addMember("message = %S", deprecation.message)
        .addMember("level = %T.%L", DeprecationLevel::class, deprecation.level)
        .build()
  }

  private val suppressDeprecation = feature.deprecation
      ?.let {
        when (it.level) {
          WARNING -> "DEPRECATION"
          ERROR, HIDDEN -> "DEPRECATION_ERROR"
        }
      }
      ?.let { name ->
        AnnotationSpec.builder(Suppress::class)
            .addMember("%S", name)
            .build()
      }

  private val defaultOptionProperty = feature.options.toList()
      .single { @Kt41142 it.isDefault }
      .let { option ->
        PropertySpec
            .builder(defaultOptionPropertyName, feature.className, OVERRIDE)
            .apply { suppressDeprecation?.let { @Kt41142 addAnnotation(it) } }
            .getter(FunSpec.getterBuilder().addCode("return %L", option.name).build())
            .build()
      }

  private val suppressCast = AnnotationSpec.builder(Suppress::class)
      .addMember("%S", "UNCHECKED_CAST")
      .build()

  private val sourceProperty = feature.source?.let { nestedSource ->
    nestedSource to PropertySpec
        .builder(sourcePropertyName, featureClassType, OVERRIDE)
        .addAnnotation(suppressCast)
        .initializer("%T::class.java as %T", nestedSource.className, featureClassType)
        .build()
  }

  private val description: String? = feature.description.takeIf { @Kt41142 it.isNotBlank() }

  private val kdoc = description?.prepareKdocHyperlinks()?.let(CodeBlock::of)

  private val descriptionProperty = description?.let { description ->
    PropertySpec
        .builder(descriptionPropertyName, String::class, OVERRIDE)
        .initializer("%S", description)
        .build()
  }

  private val supervisorOptionProperty = feature.supervisor?.let { supervisor ->
    PropertySpec
        .builder(supervisorOptionPropertyName, featureType, OVERRIDE)
        .initializer("%T.%L", supervisor.featureFlag.className, supervisor.option.name)
        .build()
  }

  private val typeSpec: TypeSpec = TypeSpec.enumBuilder(feature.className)
      .apply { deprecated?.let { @Kt41142 addAnnotation(it) } }
      .addModifiers(feature.visibility.modifier)
      .apply {
        var parametrizedType: TypeName = feature.className
        if (suppressDeprecation != null) {
          parametrizedType = parametrizedType.copy(annotations = listOf(suppressDeprecation))
        }
        addSuperinterface(Feature::class(parametrizedType))
      }
      .addProperty(defaultOptionProperty)
      .apply {
        feature.options.foldLeft(this) { builder, featureOption ->
          builder.addEnumConstant(featureOption.name)
        }
      }
      .apply {
        sourceProperty?.let { (nestedSource, sourceWithOverride) ->
          addType(FeatureFlagGenerator(nestedSource).typeSpec)
          addProperty(sourceWithOverride)
        }
      }
      .apply { kdoc?.let { @Kt41142 addKdoc(it) } }
      .apply { descriptionProperty?.let { @Kt41142 addProperty(it) } }
      .apply { supervisorOptionProperty?.let { @Kt41142 addProperty(it) } }
      .build()

  private val fileSpec = FileSpec.builder(feature.packageName, feature.name)
      .addType(typeSpec)
      .build()

  fun generate(output: File) = fileSpec.writeTo(output)

  private companion object {
    const val defaultOptionPropertyName = "defaultOption"
    const val sourcePropertyName = "source"
    const val descriptionPropertyName = "description"
    const val supervisorOptionPropertyName = "supervisorOption"

    val featureType = Feature::class(STAR)
    val featureClassType = Class::class(featureType)

    operator fun KClass<*>.invoke(parameter: TypeName) = asClassName().parameterizedBy(parameter)
  }
}
