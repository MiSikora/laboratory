package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import io.mehow.laboratory.Feature
import io.mehow.laboratory.generator.TextToken.Link
import io.mehow.laboratory.generator.TextToken.Regular

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

  private val suppressDeprecation = feature.deprecation?.suppressSpec

  private val defaultOptionProperty = feature.options.toList()
      .single(FeatureFlagOption::isDefault)
      .let { option ->
        PropertySpec
            .builder(defaultOptionPropertyName, feature.className, OVERRIDE)
            .apply { suppressDeprecation?.let { addAnnotation(it) } }
            .getter(FunSpec.getterBuilder().addCode("return %L", option.name).build())
            .build()
      }

  private val sourceProperty = feature.source?.let { nestedSource ->
    nestedSource to PropertySpec
        .builder(sourcePropertyName, featureClassType, OVERRIDE)
        .initializer("%T::class.java", nestedSource.className)
        .build()
  }

  private val description: String? = feature.description.takeIf(String::isNotBlank)

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
      .apply { deprecated?.let { addAnnotation(it) } }
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
        feature.options.fold(this) { builder, featureOption ->
          builder.addEnumConstant(featureOption.name)
        }
      }
      .apply {
        sourceProperty?.let { (nestedSource, sourceWithOverride) ->
          addType(FeatureFlagGenerator(nestedSource).typeSpec)
          addProperty(sourceWithOverride)
        }
      }
      .apply { kdoc?.let { addKdoc(it) } }
      .apply { descriptionProperty?.let { addProperty(it) } }
      .apply { supervisorOptionProperty?.let { addProperty(it) } }
      .build()

  private val fileSpec = FileSpec.builder(feature.className.packageName, feature.className.simpleName)
      .addType(typeSpec)
      .build()

  fun fileSpec() = fileSpec

  private companion object {
    const val defaultOptionPropertyName = "defaultOption"
    const val sourcePropertyName = "source"
    const val descriptionPropertyName = "description"
    const val supervisorOptionPropertyName = "supervisorOption"

    val featureType = Feature::class(STAR)
    val featureClassType = Class::class(WildcardTypeName.producerOf(featureType))
  }
}

private val extractLinkRegex = """\[([^\[\]]+)]\(([^()]+)\)""".toRegex()

// TODO: https://github.com/MiSikora/laboratory/issues/71
internal fun String.prepareKdocHyperlinks(): String {
  val matches = extractLinkRegex.findAll(this)
  val regularTokens = matches.toRegularTokens(this)
  val linkTokens = matches.toLinkTokens()
  val tokens = (regularTokens + linkTokens)
      .sortedBy { (_, startIndex) -> startIndex }
      .map { (token, _) -> token }
  return buildString {
    for (token in tokens) {
      token.append(this)
    }
  }
}

private sealed class TextToken {
  abstract fun append(builder: StringBuilder)

  data class Regular(private val text: String) : TextToken() {
    override fun append(builder: StringBuilder) {
      builder.append(text)
    }
  }

  data class Link(private val text: String, private val url: String) : TextToken() {
    override fun append(builder: StringBuilder) {
      builder.append('[')
      builder.append(text.replace(' ', '·'))
      builder.append(']')
      builder.append('(')
      builder.append(url)
      builder.append(')')
    }
  }
}

private fun Sequence<MatchResult>.toLinkTokens() = map { matchResult ->
  val (text, url) = matchResult.destructured
  Link(text, url) to matchResult.range.first
}

private fun Sequence<MatchResult>.toRegularTokens(text: String) = toUnmatchedRanges(text)
    .map { range -> Regular(text.substring(range)) to range.first }

private fun Sequence<MatchResult>.toUnmatchedRanges(text: String) = sequence {
  yield(Int.MIN_VALUE..0)
  yieldAll(map { it.range }.map { it.first - 1..it.last + 1 })
  yield(text.length - 1..Int.MAX_VALUE)
}.windowed(2, 1).map { (start, end) -> start.last..end.first }.filterNot { range -> range.isEmpty() }
