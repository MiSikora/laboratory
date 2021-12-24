package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import io.mehow.laboratory.generator.GenerationFailure.MissingOption
import io.mehow.laboratory.generator.GenerationFailure.SelfSupervision
import io.mehow.laboratory.generator.Visibility.Internal

internal class SupervisorSpec : DescribeSpec({
  val featureBuilder = FeatureFlagModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow", "FeatureA"),
      options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
  )

  context("supervisor option") {
    it("must be present in parent") {
      checkAll(Arb.stringPattern("[a-z](0)([a-z]{0,10})")) { optionName ->
        val option = FeatureFlagOption(optionName, isDefault = true)
        val className = ClassName(featureBuilder.className.packageName, "Parent")
        val parent = featureBuilder.copy(className = className, options = listOf(option))
            .build()
            .shouldBeRight()
        val builder = Supervisor.Builder(parent, option)

        val result = builder.build()

        result.shouldBeRight()
      }
    }

    it("cannot be absent in parent") {
      checkAll(Arb.stringPattern("[a-z](0)([a-z]{0,10})")) { optionName ->
        val className = ClassName(featureBuilder.className.packageName, "Parent")
        val parent = featureBuilder.copy(className = className).build().shouldBeRight()
        val option = FeatureFlagOption(optionName)
        val builder = Supervisor.Builder(parent, option)

        val result = builder.build()

        result shouldBeLeft MissingOption(parent.toString(), optionName)
      }
    }
  }
})
