package io.mehow.laboratory.generator

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
import io.mehow.laboratory.generator.Visibility.Internal

public class OptionFactoryModel(
  public val className: ClassName,
  public val features: List<FeatureFlagModel>,
  public val visibility: Visibility = Internal,
) {
  init {
    requireNoDuplicates()
  }

  public fun prepare(): FileSpec = OptionFactoryGenerator(this).fileSpec

  private companion object {
    fun OptionFactoryModel.requireNoDuplicates() {
      val groupedFeatures = features.groupBy { it.key ?: it.className.canonicalName }
      require(groupedFeatures.size == features.size) {
        val duplicates = groupedFeatures
            .filterValues { it.size > 1 }
            .mapValues { (_, features) -> features.map(FeatureFlagModel::toString) }
        """
        |Feature flags must have unique keys. Found following duplicates:
        | - ${duplicates.toList().joinToString(separator = "\n - ") { (key, fqcns) -> "$key: $fqcns" }}
      """.trimMargin()
      }
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
