package io.mehow.laboratory

import app.cash.turbine.test
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

internal class ParentChildFeatureSpec : DescribeSpec({
  describe("parent feature") {
    it("supervises experiments on child") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOption(FirstChildFeature.B)
      laboratory.setOption(ParentFeature.B)

      laboratory.experiment<FirstChildFeature>() shouldBe FirstChildFeature.A
    }

    it("does not change saved child option") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOption(FirstChildFeature.B)
      laboratory.setOption(ParentFeature.B)
      laboratory.setOption(ParentFeature.A)

      laboratory.experiment<FirstChildFeature>() shouldBe FirstChildFeature.B
    }

    it("does not disable changing child option") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOption(ParentFeature.B)
      laboratory.setOption(FirstChildFeature.B)
      laboratory.setOption(ParentFeature.A)

      laboratory.experiment<FirstChildFeature>() shouldBe FirstChildFeature.B
    }

    it("supervises observation of child") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOption(FirstChildFeature.B)

      laboratory.observe<FirstChildFeature>().test {
        expectItem() shouldBe FirstChildFeature.B

        laboratory.setOption(ParentFeature.B)
        expectItem() shouldBe FirstChildFeature.A

        laboratory.setOption(ParentFeature.A)
        expectItem() shouldBe FirstChildFeature.B

        cancel()
      }
    }

    it("prevents child from emitting same value twice") {
      val laboratory = Laboratory.inMemory()

      laboratory.observe<FirstChildFeature>().test {
        expectItem() shouldBe FirstChildFeature.A

        laboratory.setOption(ParentFeature.B)
        expectNoEvents()

        cancel()
      }
    }
  }

  describe("grandparent feature") {
    it("supervises experiments on grandchild") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOption(SecondChildFeature.B)
      laboratory.setOption(ParentFeature.B)
      laboratory.setOption(GrandParentFeature.B)

      laboratory.experiment<SecondChildFeature>() shouldBe SecondChildFeature.A
    }

    it("supervises observation of grandchild") {
      val laboratory = Laboratory.inMemory()

      laboratory.setOption(SecondChildFeature.B)
      laboratory.setOption(ParentFeature.B)

      laboratory.observe<SecondChildFeature>().test {
        expectItem() shouldBe SecondChildFeature.B

        laboratory.setOption(GrandParentFeature.B)
        expectItem() shouldBe SecondChildFeature.A

        laboratory.setOption(GrandParentFeature.A)
        expectItem() shouldBe SecondChildFeature.B

        cancel()
      }
    }
  }
})



