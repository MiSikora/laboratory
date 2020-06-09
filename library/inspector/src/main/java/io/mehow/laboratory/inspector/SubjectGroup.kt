package io.mehow.laboratory.inspector

internal class SubjectGroup(
  val name: String,
  val models: List<SubjectModel>
) {
  val hasSubjects = models.isNotEmpty()
}
