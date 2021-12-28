package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.AnnotationSpec
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.HIDDEN
import kotlin.DeprecationLevel.WARNING

public class Deprecation(
  public val message: String,
  public val level: DeprecationLevel = WARNING,
) {
  internal val suppressSpec = when (level) {
    WARNING -> "DEPRECATION"
    ERROR, HIDDEN -> "DEPRECATION_ERROR"
  }.let { name ->
    AnnotationSpec.builder(Suppress::class)
        .addMember("%S", name)
        .build()
  }
}
