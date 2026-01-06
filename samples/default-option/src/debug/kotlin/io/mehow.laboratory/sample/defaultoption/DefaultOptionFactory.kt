package io.mehow.laboratory.sample.defaultoption

import io.mehow.laboratory.DefaultOptionFactory
import io.mehow.laboratory.Feature

fun DefaultOptionFactory.Companion.create(): DefaultOptionFactory = DebugDefaultOptionFactory

private object DebugDefaultOptionFactory : DefaultOptionFactory {
  override fun create(feature: Feature<*>) =
    feature::class.java.enumConstants!!.firstOrNull { it.name == "Disabled" }
}
