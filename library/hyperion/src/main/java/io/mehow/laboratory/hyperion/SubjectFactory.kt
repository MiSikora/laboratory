package io.mehow.laboratory.hyperion

interface SubjectFactory {
  fun create(): Set<Class<Enum<*>>>

  companion object
}
