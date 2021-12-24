package io.mehow.laboratory.generator

import arrow.core.Either
import arrow.core.computations.either
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.joinToCode
import io.mehow.laboratory.Feature
import io.mehow.laboratory.OptionFactory
import io.mehow.laboratory.generator.GenerationFailure.DuplicateKeys

public class OptionFactoryModel internal constructor(
  internal val visibility: Visibility,
  internal val className: ClassName,
  internal val features: List<FeatureFlagModel>,
) {
  public fun prepare(): FileSpec = OptionFactoryGenerator(this).fileSpec

  public data class Builder(
    private val visibility: Visibility,
    private val className: ClassName,
    private val features: List<FeatureFlagModel>,
  ) {
    public fun build(): Either<GenerationFailure, OptionFactoryModel> = either.eager {
      val features = validateFeatures().bind()
      OptionFactoryModel(visibility, className, features)
    }

    private fun validateFeatures(): Either<GenerationFailure, List<FeatureFlagModel>> = run {
      val groupedFeatures = features.groupBy { it.key ?: it.className.canonicalName }
      Either.conditionally(
          test = groupedFeatures.size == features.size,
          ifTrue = { features },
          ifFalse = {
            val duplicates = groupedFeatures
                .filterValues { it.size > 1 }
                .mapValues { (_, features) -> features.map(FeatureFlagModel::toString) }
            DuplicateKeys(duplicates)
          }
      )
    }
  }
}

private class OptionFactoryGenerator(
  private val model: OptionFactoryModel,
) {
  private val nameMatcher = model.features.associateBy { it.className }
      .mapValues { (className, feature) ->
        val whenExpression = feature.options
            .map { CodeBlock.of("%S·->·%T.%L", it.name, className, it.name) }
            .joinToCode(prefix = "when·(name)·{\n⇥", separator = "\n", suffix = "\nelse·->·null⇤\n}")
        val deprecation = feature.deprecation?.suppressSpec
        if (deprecation != null) {
          CodeBlock.of("%L·%L", deprecation, whenExpression)
        } else {
         whenExpression
        }
      }

  private val keyMatcher = model.features
      .sortedWith(compareBy({ it.key == null }, { it.key }, { it.className.canonicalName }))
      .map { CodeBlock.of("%S·->·%L", it.key ?: it.className.canonicalName, nameMatcher.getValue(it.className)) }
      .joinToCode(prefix = "when·(key)·{\n⇥", separator = "\n", suffix = "\nelse·->·null⇤\n}")

  private val createFunctionOverride = FunSpec.builder("create")
      .addModifiers(OVERRIDE)
      .addParameter("key", String::class)
      .addParameter("name", String::class)
      .returns(Feature::class(STAR).copy(nullable = true))
      .apply { if (model.features.isEmpty()) addStatement("return null") else addStatement("return %L", keyMatcher) }
      .build()

  private val factoryType = TypeSpec.objectBuilder(model.className)
      .addModifiers(PRIVATE)
      .addSuperinterface(OptionFactory::class)
      .addFunction(createFunctionOverride)
      .build()

  private val factoryExtension = FunSpec.builder("generated")
      .addModifiers(model.visibility.modifier)
      .receiver(OptionFactory.Companion::class)
      .returns(OptionFactory::class)
      .addStatement("return %N", factoryType)
      .build()

  val fileSpec = FileSpec.builder(model.className.packageName, model.className.simpleName)
      .addFunction(factoryExtension)
      .addType(factoryType)
      .build()
}
