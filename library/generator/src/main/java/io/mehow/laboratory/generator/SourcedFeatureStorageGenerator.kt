package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.joinToCode
import io.mehow.laboratory.FeatureStorage
import java.io.File
import java.util.Locale

internal class SourcedFeatureStorageGenerator(
  storage: SourcedFeatureStorageModel,
) {
  private val sourceNames = storage.sourceNames
      .filterNot { featureName -> featureName.equals("local", ignoreCase = true) }
      .distinct()

  private val sourced = MemberName(FeatureStorage.Companion::class.asClassName(), "sourced")

  private val mapOf = MemberName("kotlin.collections", "mapOf")

  private val emptyMap = MemberName("kotlin.collections", "emptyMap")

  private val infixTo = MemberName("kotlin", "to")

  private val storageExtension = FunSpec.builder("sourcedGenerated")
      .addModifiers(storage.visibility.modifier)
      .receiver(FeatureStorage.Companion::class)
      .returns(FeatureStorage::class)
      .addParameter(localSourceParam, FeatureStorage::class)
      .apply {
        val parameterNames = sourceNames.map { "${it.decapitalize(Locale.ROOT)}Source" }
        for (name in parameterNames) {
          addParameter(name, FeatureStorage::class)
        }

        if (sourceNames.isNotEmpty()) {
          val remoteSources = sourceNames.zip(parameterNames) { name, parameterName ->
            CodeBlock.of("%S %M %L", name, infixTo, parameterName)
          }.joinToCode(prefix = "\n⇥", separator = ",\n", suffix = "⇤\n")
          addStatement("return %M(\n⇥%L,\n%M(%L)⇤\n)", sourced, localSourceParam, mapOf, remoteSources)
        } else {
          addStatement("return %M(\n⇥%L,\n%M()⇤\n)", sourced, localSourceParam, emptyMap)
        }
      }
      .build()

  private val storageFile = FileSpec.builder(storage.packageName, storage.name)
      .addFunction(storageExtension)
      .build()

  fun generate(file: File) = storageFile.writeTo(file)

  private companion object {
    const val localSourceParam = "localSource"
  }
}
