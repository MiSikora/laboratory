package io.mehow.laboratory.hyperion

internal class SubjectModel(
  val subject: Enum<*>,
  val isSelected: Boolean
) {
  fun select() = SubjectModel(subject, true)
}
