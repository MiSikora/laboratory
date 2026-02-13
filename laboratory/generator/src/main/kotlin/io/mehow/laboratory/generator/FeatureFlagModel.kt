package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import io.mehow.laboratory.BinaryFeature
import io.mehow.laboratory.Feature
import io.mehow.laboratory.generator.TextToken.Link
import io.mehow.laboratory.generator.TextToken.Regular
import io.mehow.laboratory.generator.Visibility.Public

public class FeatureFlagModel(
  public val className: ClassName,
  public val options: List<FeatureFlagOption>,
  public val visibility: Visibility = Public,
  public val isRemote: Boolean = false,
  public val isRemoteValueDefault: Boolean = true,
  public val description: String = "",
  public val deprecation: Deprecation? = null,
  public val key: String? = null,
  public val trueOption: FeatureFlagTrueOption? = null,
) {
  init {
    require(options.isNotEmpty()) { "${className.canonicalName} must have at least one option" }
    require(options.count(FeatureFlagOption::isDefault) == 1) {
      "${className.canonicalName} must have exactly one default option"
    }
    if (trueOption != null) {
      val optionNames = options.map(FeatureFlagOption::name)
      require(options.size == 2) {
        "${className.canonicalName} must have exactly two options. Found: $optionNames"
      }
      require(trueOption.name in options.map(FeatureFlagOption::name)) {
        "${className.canonicalName} has unknown 'true' option. Options: $optionNames, True option: ${trueOption.name}"
      }
    }
  }

  public fun prepare(): FileSpec = FeatureFlagGenerator(this).fileSpec()

  override fun equals(other: Any?): Boolean =
    other is FeatureFlagModel && className.reflectionName() == other.className.reflectionName()

  override fun hashCode(): Int = className.reflectionName().hashCode()

  override fun toString(): String = className.canonicalName
}

private class FeatureFlagGenerator(private val feature: FeatureFlagModel) {
  private val deprecated =
    feature.deprecation?.let { deprecation ->
      AnnotationSpec.builder(Deprecated::class)
        .addMember("message = %S", deprecation.message)
        .addMember("level = %T.%L", DeprecationLevel::class, deprecation.level)
        .build()
    }

  private val suppressDeprecation = feature.deprecation?.suppressSpec

  private val defaultOptionProperty =
    feature.options.toList().single(FeatureFlagOption::isDefault).let { option ->
      PropertySpec.builder(defaultOptionPropertyName, feature.className, OVERRIDE)
        .apply { suppressDeprecation?.let { addAnnotation(it) } }
        .getter(FunSpec.getterBuilder().addCode("return %L", option.name).build())
        .build()
    }

  private val defaultSourceProperty =
    if (feature.isRemote && feature.isRemoteValueDefault) {
      PropertySpec.builder(defaultSourcePropertyName, sourceType, OVERRIDE)
        .getter(
          FunSpec.getterBuilder().addCode("return %T.%L", sourceType, Feature.Source.Remote).build()
        )
        .build()
    } else {
      null
    }

  private val description: String? = feature.description.takeIf(String::isNotBlank)

  private val isRemote: Boolean? = feature.isRemote.takeIf { it }

  private val isRemoteValueDefault: Boolean? = feature.isRemoteValueDefault.takeIf { it.not() }

  private val kdocCodeBlock = description?.prepareKdocHyperlinks()?.let(CodeBlock::of)

  private val descriptionProperty =
    description?.let { description ->
      PropertySpec.builder(descriptionPropertyName, String::class, OVERRIDE)
        .initializer("%S", description)
        .build()
    }

  private val isRemoteProperty =
    isRemote?.let { isRemote ->
      PropertySpec.builder(isRemotePropertyName, Boolean::class, OVERRIDE)
        .getter(FunSpec.getterBuilder().addCode("return %L", isRemote).build())
        .build()
    }

  private val isRemoteValueDefaultProperty =
    isRemoteValueDefault?.let { isRemoteValueDefault ->
      PropertySpec.builder(isRemoteValueDefaultPropertyName, Boolean::class, OVERRIDE)
        .getter(FunSpec.getterBuilder().addCode("return %L", isRemoteValueDefault).build())
        .build()
    }

  private val binaryFeatureConstructor =
    if (feature.trueOption != null) {
      FunSpec.constructorBuilder().addParameter(binaryValuePropertyName, Boolean::class).build()
    } else {
      null
    }

  private val binaryValueProperty =
    if (binaryFeatureConstructor != null) {
      PropertySpec.builder(binaryValuePropertyName, Boolean::class, OVERRIDE)
        .initializer(binaryValuePropertyName)
        .build()
    } else {
      null
    }

  private val typeSpec: TypeSpec =
    TypeSpec.enumBuilder(feature.className)
      .apply { deprecated?.let(::addAnnotation) }
      .addModifiers(feature.visibility.modifier)
      .apply {
        var parametrizedType: TypeName = feature.className
        if (suppressDeprecation != null) {
          parametrizedType = parametrizedType.copy(annotations = listOf(suppressDeprecation))
        }
        val superType =
          if (feature.trueOption != null) {
            BinaryFeature::class
          } else {
            Feature::class
          }
        addSuperinterface(superType(parametrizedType))
      }
      .apply { binaryFeatureConstructor?.let(::primaryConstructor) }
      .apply { binaryValueProperty?.let(::addProperty) }
      .addProperty(defaultOptionProperty)
      .apply {
        feature.options.fold(this) { builder, featureOption ->
          val typeSpec =
            TypeSpec.anonymousClassBuilder()
              .apply {
                if (feature.trueOption != null) {
                  addSuperclassConstructorParameter(
                    "%L",
                    featureOption.name == feature.trueOption.name,
                  )
                }
              }
              .build()
          builder.addEnumConstant(featureOption.name, typeSpec)
        }
      }
      .apply { defaultSourceProperty?.let(::addProperty) }
      .apply { kdocCodeBlock?.let(::addKdoc) }
      .apply { descriptionProperty?.let(::addProperty) }
      .apply { isRemoteProperty?.let(::addProperty) }
      .apply { isRemoteValueDefaultProperty?.let(::addProperty) }
      .build()

  private val fileSpec =
    FileSpec.builder(feature.className.packageName, feature.className.simpleName)
      .addType(typeSpec)
      .build()

  fun fileSpec() = fileSpec

  private companion object {
    const val defaultOptionPropertyName = "defaultOption"
    const val defaultSourcePropertyName = "defaultSource"
    const val descriptionPropertyName = "description"
    const val binaryValuePropertyName = "binaryValue"
    const val isRemotePropertyName = "isRemote"
    const val isRemoteValueDefaultPropertyName = "isRemoteValueDefault"

    val sourceType = Feature.Source::class
  }
}

public data class FeatureFlagOption(public val name: String, public val isDefault: Boolean = false)

public data class FeatureFlagTrueOption(public val name: String)

private val extractLinkRegex = """\[([^\[\]]+)]\(([^()]+)\)""".toRegex()

// TODO: https://github.com/MiSikora/laboratory/issues/71
internal fun String.prepareKdocHyperlinks(): String {
  val matches = extractLinkRegex.findAll(this)
  val regularTokens = matches.toRegularTokens(this)
  val linkTokens = matches.toLinkTokens()
  val tokens =
    (regularTokens + linkTokens)
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

private fun Sequence<MatchResult>.toRegularTokens(text: String) =
  toUnmatchedRanges(text).map { range -> Regular(text.substring(range)) to range.first }

private fun Sequence<MatchResult>.toUnmatchedRanges(text: String) =
  sequence {
      yield(Int.MIN_VALUE..0)
      yieldAll(map { it.range }.map { it.first - 1..it.last + 1 })
      yield(text.length - 1..Int.MAX_VALUE)
    }
    .windowed(2, 1)
    .map { (start, end) -> start.last..end.first }
    .filterNot { range -> range.isEmpty() }
