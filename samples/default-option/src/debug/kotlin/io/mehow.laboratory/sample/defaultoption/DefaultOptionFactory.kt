package io.mehow.laboratory.sample.defaultoption

import io.mehow.laboratory.DefaultOptionFactory
import io.mehow.laboratory.Feature
import io.mehow.laboratory.options

fun DefaultOptionFactory.Companion.create(): DefaultOptionFactory = DebugDefaultOptionFactory

private object DebugDefaultOptionFactory : DefaultOptionFactory {
  override fun <T : Feature<out T>> create(feature: T) = feature::class.java
      .options
      .firstOrNull { it.name == "Disabled" }
}
