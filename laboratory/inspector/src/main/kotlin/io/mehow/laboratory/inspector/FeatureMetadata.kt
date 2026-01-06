package io.mehow.laboratory.inspector

import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.options
import java.util.concurrent.atomic.AtomicReference

internal class FeatureMetadata(
  val id: Long,
  val type: Class<Feature<*>>,
  val name: String,
  val options: List<Feature<*>>,
  val defaultSource: Feature.Source,
  val description: List<TextToken>,
  val style: FeatureStyle,
  val alignment: FeatureAlignment,
) {
  private val nameQuery = QueryString.create(name)
  private val optionQueries = options.map { QueryString.create(it.name) }

  fun matches(query: QueryString) = query in nameQuery || optionQueries.any { query in it }

  override fun equals(other: Any?) = other is FeatureMetadata && type == other.type

  override fun hashCode() = type.hashCode()

  class Loader(
    private val featureFactory: FeatureFactory,
    private val deprecationHandler: DeprecationHandler,
  ) {
    private val metadataRef = AtomicReference<List<FeatureMetadata>>()

    fun load(): List<FeatureMetadata> {
      var value = metadataRef.get()
      if (value == null) {
        synchronized(this) {
          value = metadataRef.get() ?: createMetadata()
          metadataRef.set(value)
        }
      }
      return value
    }

    private fun createMetadata() =
      featureFactory.create().mapIndexedNotNull(::createMetadata).sortedWith(Comparator)

    private fun createMetadata(index: Int, type: Class<Feature<*>>): FeatureMetadata? {
      val level = type.annotations.filterIsInstance<Deprecated>().firstOrNull()?.level
      val style = level?.let(deprecationHandler::getPhenotype) ?: FeatureStyle.Show
      return if (style != FeatureStyle.Hide) {
        val options = type.options
        return FeatureMetadata(
          id = index.toLong(),
          type = type,
          name = type.name.substringAfterLast('.').replace('$', '.'),
          options = options.toList(),
          defaultSource = options[0].defaultSource,
          description = options[0].description.tokenize(),
          style = style,
          alignment = level?.let(deprecationHandler::getAlignment) ?: FeatureAlignment.Regular,
        )
      } else {
        null
      }
    }
  }
}

internal fun List<FeatureMetadata>.matches(query: QueryString) = filter { metadata ->
  metadata.matches(query)
}

private val Comparator = compareBy(FeatureMetadata::alignment, FeatureMetadata::name)
