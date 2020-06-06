package io.mehow.laboratory

interface SubjectStorage {
  fun <T : Enum<*>> getSubjectName(group: Class<T>): String?
  fun <T : Enum<*>> setSubject(subject: T)

  companion object {
    fun inMemory() = object : SubjectStorage {
      private val subjects = mutableMapOf<String, String>()

      override fun <T : Enum<*>> getSubjectName(group: Class<T>) = subjects[group.name]

      override fun <T : Enum<*>> setSubject(subject: T) {
        subjects[subject.javaClass.name] = subject.name
      }
    }
  }
}
