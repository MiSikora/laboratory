package io.mehow.laboratory.internal

import io.mehow.laboratory.Feature
import kotlin.collections.getOrPut

private val cache = mutableMapOf<Class<*>, List<*>>()

@InternalLaboratoryApi
public val <T> Class<out T>.options: List<T> where T : Feature<T>, T : Enum<out T>
  get() {
    val options =
      cache.getOrPut(this) {
        val enums = requireNotNull(enumConstants) { "$canonicalName must be an enum" }
        require(enums.isNotEmpty()) { "$canonicalName must have at least one option" }
        require(enums.first() is T) { "$canonicalName must be a feature" }
        enums.toList()
      }
    @Suppress("UNCHECKED_CAST")
    return options as List<T>
  }

@InternalLaboratoryApi
public val <T> Class<out T>.firstOption: T where T : Feature<T>, T : Enum<out T>
  get() = options.first()

@InternalLaboratoryApi
public val <T> Class<out T>.defaultOption: T where T : Feature<T>, T : Enum<out T>
  get() = firstOption.defaultOption

@InternalLaboratoryApi
public val <T> Class<out T>.defaultSource: Feature.Source where T : Feature<T>, T : Enum<out T>
  get() = firstOption.defaultSource

@InternalLaboratoryApi
public val Class<out Feature<*>>.optionsRaw: List<Feature<*>>
  get() {
    val options =
      cache.getOrPut(this) {
        val enums = requireNotNull(enumConstants) { "$canonicalName must be an enum" }
        require(enums.isNotEmpty()) { "$canonicalName must have at least one option" }
        require(enums.first() is Feature<*>) { "$canonicalName must be a feature" }
        enums.toList()
      }
    @Suppress("UNCHECKED_CAST")
    return options as List<Feature<*>>
  }

@InternalLaboratoryApi
public val Class<out Feature<*>>.firstOptionRaw: Feature<*>
  get() = optionsRaw.first()

@InternalLaboratoryApi
public val Class<out Feature<*>>.defaultOptionRaw: Feature<*>
  get() = firstOptionRaw.defaultOption

@InternalLaboratoryApi
public val Class<out Feature<*>>.defaultSourceRaw: Feature.Source
  get() = firstOptionRaw.defaultSource
