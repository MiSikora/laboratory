package io.mehow.laboratory.compiler

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode
import io.mehow.laboratory.FeatureFactory
import java.io.File
import kotlin.reflect.KClass

internal class FeatureFactoryGenerator(
  factory: FeatureFactoryModel
) {
  private val featureClasses = factory.features
    .map(FeatureFlagModel::fqcn)
    .sorted()
    .map { name -> CodeBlock.of("%T.forName(%S)", Class::class.asTypeName(), name) }
    .joinToCode(prefix = "\n⇥", separator = ",\n", suffix = "⇤\n")

  private val suppressCast = AnnotationSpec.builder(Suppress::class)
    .addMember("%S", "UNCHECKED_CAST")
    .build()

  private val setOf = MemberName("kotlin.collections", "setOf")

  private val emptySet = MemberName("kotlin.collections", "emptySet")

  private val discoveryFunctionOverride = FunSpec.builder("create")
    .addModifiers(OVERRIDE)
    .apply {
      if (factory.features.isNotEmpty()) {
        addAnnotation(suppressCast)
        addStatement("return %M(%L) as %T", setOf, featureClasses, factoryReturnType)
      } else addStatement("return %M<%T>()", emptySet, setType)
    }
    .build()

  private val factoryType = TypeSpec.objectBuilder(factory.name)
    .addModifiers(PRIVATE)
    .addSuperinterface(FeatureFactory::class)
    .addFunction(discoveryFunctionOverride)
    .build()

  private val factoryExtension = FunSpec.builder("generated")
    .addModifiers(factory.visibility.modifier)
    .receiver(FeatureFactory.Companion::class)
    .returns(FeatureFactory::class)
    .addStatement("return %N", factoryType)
    .build()

  private val factoryFile = FileSpec.builder(factory.packageName, factory.name)
    .addFunction(factoryExtension)
    .addType(factoryType)
    .build()

  fun generate(file: File) = factoryFile.writeTo(file)

  private companion object {
    val setType = Class::class(Enum::class(STAR))
    val factoryReturnType = Set::class(setType)

    operator fun KClass<*>.invoke(parameter: TypeName) = asClassName().parameterizedBy(parameter)
  }
}
