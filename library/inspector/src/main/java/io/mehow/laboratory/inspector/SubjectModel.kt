package io.mehow.laboratory.inspector

internal class SubjectModel(
  val subject: Enum<*>,
  val isSelected: Boolean
) {
  fun select() = SubjectModel(subject, true)
}
