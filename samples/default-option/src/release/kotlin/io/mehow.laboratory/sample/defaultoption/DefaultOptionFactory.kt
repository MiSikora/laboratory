package io.mehow.laboratory.sample.defaultoption

import io.mehow.laboratory.DefaultOptionFactory
import io.mehow.laboratory.Feature

fun DefaultOptionFactory.Companion.create() = object : DefaultOptionFactory {
  override fun <T : Feature<out T>> create(feature: T) = null
}
