package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory

internal class FeatureFactoryGenerator(
  factory: FeatureFactoryModel,
  functionName: String,
) {
  private val featureClasses = factory.features
      .map { it.className.reflectionName() }
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
        } else addStatement("return %M<%T>()", emptySet, featureType)
      }
      .build()

  private val factoryType = TypeSpec.objectBuilder(factory.className)
      .addModifiers(PRIVATE)
      .addSuperinterface(FeatureFactory::class)
      .addFunction(discoveryFunctionOverride)
      .build()

  private val factoryExtension = FunSpec.builder(functionName)
      .addModifiers(factory.visibility.modifier)
      .receiver(FeatureFactory.Companion::class)
      .returns(FeatureFactory::class)
      .addStatement("return %N", factoryType)
      .build()

  private val factoryFile = FileSpec.builder(factory.className.packageName, factory.className.simpleName)
      .addFunction(factoryExtension)
      .addType(factoryType)
      .build()

  fun fileSpec() = factoryFile

  private companion object {
    val featureType = Class::class(Feature::class(STAR))
    val factoryReturnType = Set::class(featureType)
  }
}
