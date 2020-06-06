package io.mehow.laboratory.hyperion

import io.mehow.laboratory.SubjectFactory
import io.mehow.laboratory.SubjectStorage

internal class Presenter(
  factory: SubjectFactory,
  private val storage: SubjectStorage
) {
  private val groups = factory.create()

  fun getSubjectGroups(): List<SubjectGroup> {
    return groups
      .sortedBy(::groupName)
      .map(::createSubjectGroup)
      .filter(SubjectGroup::hasSubjects)
  }

  fun selectSubject(subject: Enum<*>) = storage.setSubject(subject)

  private fun groupName(group: Class<Enum<*>>): String = group.simpleName

  private fun createSubjectGroup(group: Class<Enum<*>>): SubjectGroup {
    return SubjectGroup(groupName(group), getSubjectModels(group))
  }

  private fun getSubjectModels(group: Class<Enum<*>>): List<SubjectModel> {
    val subjectName = storage.getSubjectName(group)
    return group.enumConstants
      .orEmpty()
      .map { subject -> SubjectModel(subject, isSelected = subject.name == subjectName) }
      .let(::ensureOneModelSelected)
  }

  private fun ensureOneModelSelected(models: List<SubjectModel>): List<SubjectModel> {
    return if (models.any(SubjectModel::isSelected)) models else models.selectFirst()
  }

  private fun List<SubjectModel>.selectFirst(): List<SubjectModel> {
    return take(1).map(SubjectModel::select) + drop(1)
  }
}
