package io.mehow.laboratory

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty

internal class SourcedFeatureStorage(
  private val localSource: FeatureStorage,
  private val remoteSources: Map<String, FeatureStorage>,
  defaultOptionFactory: DefaultOptionFactory? = null,
) : FeatureStorage {
  private val localLaboratory = Laboratory.builder()
      .featureStorage(localSource)
      .let { builder -> defaultOptionFactory?.let(builder::defaultOptionFactory) ?: builder }
      .build()

  override fun observeFeatureName(feature: Class<out Feature<*>>) = feature.observeSource()
      .map { source -> remoteSources[source.name] ?: localSource }
      .onEmpty { emit(localSource) }
      .let {
        @OptIn(ExperimentalCoroutinesApi::class)
        it.flatMapLatest { storage -> storage.observeFeatureName(feature) }
      }

  override suspend fun getFeatureName(feature: Class<out Feature<*>>): String? {
    val storage = feature.getSource()?.let { remoteSources[it.name] } ?: localSource
    return storage.getFeatureName(feature)
  }

  override suspend fun setOptions(vararg options: Feature<*>) = localSource.setOptions(*options)

  override suspend fun setOptions(options: Collection<Feature<*>>) = localSource.setOptions(options)

  override suspend fun clear() = localSource.clear()

  private fun <T : Feature<*>> Class<out T>.observeSource() = validatedSource()
      ?.let { localLaboratory.observe(it) }
      ?: emptyFlow()

  private suspend fun <T : Feature<*>> Class<out T>.getSource() = validatedSource()
      ?.let { localLaboratory.experiment(it) }

  private fun <T : Feature<*>> Class<out T>.validatedSource() = options
      .firstOrNull()
      ?.source
      ?.takeUnless { it.options.isEmpty() }

  fun withDefaultOptionFactory(factory: DefaultOptionFactory) = SourcedFeatureStorage(
      localSource,
      remoteSources,
      factory,
  )
}
