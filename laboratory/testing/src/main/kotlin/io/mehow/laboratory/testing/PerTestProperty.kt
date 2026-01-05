package io.mehow.laboratory.testing

import io.kotest.core.spec.Spec
import kotlin.properties.ReadOnlyProperty

fun <V> Spec.perTest(clear: (V) -> Unit = {}, initialize: () -> V): ReadOnlyProperty<Spec?, V> {
  var value: Any? = NoValue

  beforeTest { value = initialize() }

  afterTest {
    @Suppress("UNCHECKED_CAST") clear(value as V)
    value = NoValue
  }

  return ReadOnlyProperty { _, property ->
    check(value !== NoValue) { "Property '${property.name}' can be accessed only in a test case." }
    @Suppress("UNCHECKED_CAST")
    value as V
  }
}

private object NoValue
