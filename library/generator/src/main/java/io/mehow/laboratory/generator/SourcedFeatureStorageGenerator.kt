package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.DATA
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KOperator.PLUS
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import io.mehow.laboratory.FeatureStorage
import java.util.Locale

internal class SourcedFeatureStorageGenerator(
  storage: SourcedFeatureStorageModel,
) {
  private val sourceNames = storage.sourceNames
      .filterNot { featureName -> featureName.equals("local", ignoreCase = true) }
      .distinct()

  private val sourced = MemberName(FeatureStorage.Companion::class.asClassName(), "sourced")

  private val emptyMap = MemberName(kotlinCollectionsSpace, "emptyMap")

  private val mapPlus = MemberName(kotlinCollectionsSpace, PLUS)

  private val infixTo = MemberName("kotlin", "to")

  private val buildingStepClassName = ClassName(storage.className.packageName, "BuildingStep")

  private val buildingStepType = TypeSpec.interfaceBuilder(buildingStepClassName)
      .addModifiers(storage.visibility.modifier)
      .addFunction(FunSpec.builder("build")
          .addModifiers(ABSTRACT)
          .returns(FeatureStorage::class)
          .build())
      .build()

  private val remoteStepClassNames = sourceNames.distinct()
      .sorted()
      .map { ClassName(storage.className.packageName, it + stepSuffix) }

  private val remoteStepTypes = remoteStepClassNames
      .windowed(size = 2, step = 1, partialWindows = true) { sources ->
        val currentSourceClassName = sources.first()
        val functionReturnClassName = sources.drop(1).firstOrNull() ?: buildingStepClassName
        val functionName = currentSourceClassName.simpleName
            .removeSuffix(stepSuffix)
            .replaceFirstChar { it.lowercase(Locale.ROOT) } + "Source"

        TypeSpec.interfaceBuilder(currentSourceClassName)
            .addModifiers(storage.visibility.modifier)
            .addFunction(FunSpec.builder(functionName)
                .addModifiers(ABSTRACT)
                .addParameter("source", FeatureStorage::class)
                .returns(functionReturnClassName)
                .build())
            .build()
      }

  private val builderType = TypeSpec.classBuilder(ClassName(storage.className.simpleName, "Builder"))
      .addModifiers(PRIVATE, DATA)
      .addSuperinterfaces(remoteStepClassNames + buildingStepClassName)
      .primaryConstructor(FunSpec.constructorBuilder()
          .addParameter(localSourceParam, FeatureStorage::class)
          .addParameter(remoteSourcesParam, Map::class(String::class, FeatureStorage::class))
          .build())
      .addProperty(PropertySpec.builder(localSourceParam, FeatureStorage::class)
          .initializer(localSourceParam)
          .addModifiers(PRIVATE)
          .build())
      .addProperty(PropertySpec.builder(remoteSourcesParam, Map::class(String::class, FeatureStorage::class))
          .initializer(remoteSourcesParam)
          .addModifiers(PRIVATE)
          .build())
      .addFunctions(remoteStepTypes.mapIndexed { index, remoteStep ->
        val function = remoteStep.funSpecs.single()
        function.toBuilder()
            .apply { modifiers -= ABSTRACT }
            .addModifiers(OVERRIDE)
            .addStatement(
                "return copy(\n⇥%1L = %1L %2M (%3S %4M %5N)⇤\n)",
                remoteSourcesParam,
                mapPlus,
                remoteStepClassNames[index].simpleName.removeSuffix(stepSuffix),
                infixTo,
                function.parameters.single(),
            )
            .build()
      })
      .addFunction(buildingStepType.funSpecs.single()
          .toBuilder()
          .apply { modifiers -= ABSTRACT }
          .addModifiers(OVERRIDE)
          .addStatement("return %M(%L, %L)", sourced, localSourceParam, remoteSourcesParam)
          .build())
      .build()

  private val storageBuilderExtension = FunSpec.builder("sourcedBuilder")
      .addModifiers(storage.visibility.modifier)
      .receiver(FeatureStorage.Companion::class)
      .returns(remoteStepClassNames.firstOrNull() ?: buildingStepClassName)
      .addParameter(localSourceParam, FeatureStorage::class)
      .addStatement("return %N(%L, %M())", builderType, localSourceParam, emptyMap)
      .build()

  private val storageFile = FileSpec.builder(storage.className.packageName, storage.className.simpleName)
      .addFunction(storageBuilderExtension)
      .apply {
        for (type in remoteStepTypes) {
          addType(type)
        }
      }
      .addType(buildingStepType)
      .addType(builderType)
      .build()

  fun fileSpec() = storageFile

  private companion object {
    const val stepSuffix = "Step"
    const val localSourceParam = "localSource"
    const val remoteSourcesParam = "remoteSources"

    const val kotlinCollectionsSpace = "kotlin.collections"
  }
}
