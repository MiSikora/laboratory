package io.mehow.laboratory.hyperion

internal class SubjectGroup(
  val name: String,
  val models: List<SubjectModel>
) {
  val hasSubjects = models.isNotEmpty()
}
