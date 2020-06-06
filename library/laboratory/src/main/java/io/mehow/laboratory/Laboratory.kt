package io.mehow.laboratory

class Laboratory(private val storage: SubjectStorage) {
  fun <T : Enum<T>> experiment(group: Class<T>): T {
    val subjects = group.enumConstants!!
    require(subjects.isNotEmpty()) { "${group.name} must have at least one value" }
    val firstSubject = subjects.first()
    val expectedName = storage.getSubjectName(firstSubject.javaClass) ?: firstSubject.name
    return subjects.firstOrNull { it.name == expectedName } ?: firstSubject
  }

  fun <T : Enum<T>> setSubject(subject: T) {
    storage.setSubject(subject)
  }

  companion object {
    fun inMemory() = Laboratory(SubjectStorage.inMemory())
  }
}

inline fun <reified T : Enum<T>> Laboratory.experiment() = experiment(T::class.java)
