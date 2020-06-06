package io.mehow.laboratory

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class LaboratorySpec : DescribeSpec({
  describe("laboratory") {
    it("cannot use subjects without values") {
      val laboratory = Laboratory(ThrowingStorage)

      shouldThrowExactly<IllegalArgumentException> {
        laboratory.experiment<InvalidSubject>()
      } shouldHaveMessage "io.mehow.laboratory.InvalidSubject must have at least one value"
    }

    it("uses first subject by default") {
      val laboratory = Laboratory(NullStorage)

      laboratory.experiment<ValidSubject>() shouldBe ValidSubject.A
    }

    it("uses first subject if no name match found") {
      val laboratory = Laboratory(EmptyStorage)

      laboratory.experiment<ValidSubject>() shouldBe ValidSubject.A
    }

    it("uses subject saved in a storage") {
      val storage = SubjectStorage.inMemory()
      val laboratory = Laboratory(storage)

      for (subject in ValidSubject.values()) {
        storage.setSubject(subject)

        laboratory.experiment<ValidSubject>() shouldBe subject
      }
    }

    it("can directly change a subject") {
      val laboratory = Laboratory.inMemory()

      for (subject in ValidSubject.values()) {
        laboratory.setSubject(subject)

        laboratory.experiment<ValidSubject>() shouldBe subject
      }
    }
  }

  describe("in memory laboratory") {
    it("is not shared across instances") {
      val firstLaboratory = Laboratory.inMemory()
      val secondLaboratory = Laboratory.inMemory()

      firstLaboratory.setSubject(ValidSubject.B)
      firstLaboratory.experiment<ValidSubject>() shouldBe ValidSubject.B
      secondLaboratory.experiment<ValidSubject>() shouldBe ValidSubject.A

      secondLaboratory.setSubject(ValidSubject.C)
      firstLaboratory.experiment<ValidSubject>() shouldBe ValidSubject.B
      secondLaboratory.experiment<ValidSubject>() shouldBe ValidSubject.C
    }
  }
})

private enum class ValidSubject {
  A, B, C
}

private enum class InvalidSubject

private object ThrowingStorage : SubjectStorage {
  override fun <T : Enum<*>> getSubjectName(group: Class<T>) = assert()
  override fun <T : Enum<*>> setSubject(subject: T) = assert()
  private fun assert(): Nothing = throw AssertionError("Test failed!")
}

private object NullStorage : SubjectStorage {
  override fun <T : Enum<*>> getSubjectName(group: Class<T>): String? = null
  override fun <T : Enum<*>> setSubject(subject: T) = Unit
}

private object EmptyStorage : SubjectStorage {
  override fun <T : Enum<*>> getSubjectName(group: Class<T>) = ""
  override fun <T : Enum<*>> setSubject(subject: T) = Unit
}
