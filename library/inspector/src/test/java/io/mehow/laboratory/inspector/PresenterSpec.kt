package io.mehow.laboratory.inspector

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.mehow.laboratory.SubjectFactory
import io.mehow.laboratory.SubjectStorage
import io.mehow.laboratory.inspector.Presenter
import io.mehow.laboratory.inspector.SubjectGroup
import io.mehow.laboratory.inspector.SubjectModel

class PresenterSpec : DescribeSpec({
  describe("presenter") {
    it("filters empty subject groups") {
      val presenter = Presenter(
        AllSubjectFactory,
        SubjectStorage.inMemory()
      )

      val subjectNames = presenter.getSubjectGroups().map(SubjectGroup::name)

      subjectNames shouldNotContain "Empty"
    }

    it("orders subject groups by name") {
      val presenter = Presenter(
        AllSubjectFactory,
        SubjectStorage.inMemory()
      )

      val subjectNames = presenter.getSubjectGroups().map(SubjectGroup::name)

      subjectNames shouldContainExactly listOf("First", "Second")
    }

    it("does not order subject values") {
      val presenter = Presenter(
        AllSubjectFactory,
        SubjectStorage.inMemory()
      )

      val subjects = presenter.getSubjectGroups()
        .map(SubjectGroup::models)
        .map { models -> models.map(SubjectModel::subject) }

      subjects[0] shouldContainExactly listOf(
        First.C,
        First.B,
        First.A
      )
      subjects[1] shouldContainExactly listOf(
        Second.B,
        Second.C,
        Second.A
      )
    }

    it("marks first subject as selected by default") {
      val presenter = Presenter(
        AllSubjectFactory,
        SubjectStorage.inMemory()
      )

      presenter.getSelectedSubjects() shouldContainExactly listOf(
        First.C,
        Second.B
      )
    }

    it("marks saved subject as selected") {
      val storage = SubjectStorage.inMemory().apply {
        setSubject(First.A)
        setSubject(Second.C)
      }
      val presenter = Presenter(
        AllSubjectFactory,
        storage
      )

      presenter.getSelectedSubjects() shouldContainExactly listOf(
        First.A,
        Second.C
      )
    }

    it("selects subjects") {
      val presenter = Presenter(
        AllSubjectFactory,
        SubjectStorage.inMemory()
      )

      presenter.selectSubject(First.B)
      presenter.selectSubject(Second.A)

      presenter.getSelectedSubjects() shouldContainExactly listOf(
        First.B,
        Second.A
      )
    }
  }
})

internal fun Presenter.getSelectedSubjects(): List<Enum<*>> {
  return getSubjectGroups()
    .map(SubjectGroup::models)
    .map { models -> models.single(SubjectModel::isSelected).let(
      SubjectModel::subject) }
}

private object AllSubjectFactory : SubjectFactory {
  override fun create(): Set<Class<Enum<*>>> {
    @Suppress("UNCHECKED_CAST")
    return setOf(Second::class.java, First::class.java, Empty::class.java) as Set<Class<Enum<*>>>
  }
}

private enum class First {
  C, B, A
}

private enum class Second {
  B, C, A
}

private enum class Empty
