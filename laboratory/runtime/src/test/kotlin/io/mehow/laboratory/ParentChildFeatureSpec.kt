package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ParentChildFeatureSpec : FunSpec() {
  enum class GrandParentFeature : Feature<GrandParentFeature> {
    A,
    B,
    ;

    override val defaultOption get() = A
  }

  enum class ParentFeature : Feature<ParentFeature> {
    A,
    B,
    ;

    override val defaultOption get() = A

    override val supervisorOption = GrandParentFeature.A
  }

  enum class ChildFeature : Feature<ChildFeature> {
    A,
    B,
    ;

    override val defaultOption get() = A

    override val supervisorOption = ParentFeature.A
  }

  enum class ChildFeature2 : Feature<ChildFeature2> {
    A,
    B,
    ;

    override val defaultOption get() = A

    override val supervisorOption = ParentFeature.B
  }

  init {
    context("parent feature") {
      test("supervises reading child's option") {
        val laboratory = Laboratory.inMemory()

        laboratory.setOption(ChildFeature.B)
        laboratory.experiment<ChildFeature>() shouldBe ChildFeature.B

        laboratory.setOption(ParentFeature.B)
        laboratory.experiment<ChildFeature>() shouldBe ChildFeature.A
      }

      test("does not changed stored child's option") {
        val laboratory = Laboratory.inMemory()

        laboratory.setOption(ChildFeature.B)
        laboratory.setOption(ParentFeature.B)
        laboratory.setOption(ParentFeature.A)

        laboratory.experiment<ChildFeature>() shouldBe ChildFeature.B
      }

      test("does not prevent writing child's option") {
        val laboratory = Laboratory.inMemory()

        laboratory.setOption(ParentFeature.B)
        laboratory.setOption(ChildFeature.B)
        laboratory.setOption(ParentFeature.A)

        laboratory.experiment<ChildFeature>() shouldBe ChildFeature.B
      }

      test("triggers emitting child's option when they differ") {
        val laboratory = Laboratory.inMemory()

        laboratory.setOption(ChildFeature.B)

        laboratory.observe<ChildFeature>().test {
          awaitItem() shouldBe ChildFeature.B

          laboratory.setOption(ParentFeature.B)
          awaitItem() shouldBe ChildFeature.A

          laboratory.setOption(ParentFeature.A)
          awaitItem() shouldBe ChildFeature.B
        }
      }

      test("does not trigger emitting child's option when they do not differ") {
        val laboratory = Laboratory.inMemory()

        laboratory.observe<ChildFeature>().test {
          awaitItem() shouldBe ChildFeature.A

          laboratory.setOption(ParentFeature.B)
          expectNoEvents()

          laboratory.setOption(ParentFeature.A)
          expectNoEvents()
        }
      }
    }

    test("supervises reading grandchild's option") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOption(ChildFeature2.B)
      laboratory.setOption(ParentFeature.B)
      laboratory.setOption(GrandParentFeature.B)

      laboratory.experiment<ChildFeature2>() shouldBe ChildFeature2.A
    }
  }
}
