package io.mehow.laboratory

interface SubjectFactory {
  fun create(): Set<Class<Enum<*>>>

  companion object
}
