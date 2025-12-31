package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.property.Arb
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll

class SupervisorSpec :
  FunSpec({
    test("does not fail when it has an option") {
      checkAll(Arb.stringPattern("[a-z](0)([a-z]{0,10})")) { optionName ->
        val option = FeatureFlagOption(optionName, isDefault = true)
        val feature = FeatureFlagModel(ClassName("io.mehow", "FeatureA"), listOf(option))

        shouldNotThrowAny { Supervisor(feature, option) }
      }
    }

    test("fails when it has no options") {
      checkAll(Arb.stringPattern("[a-z](0)([a-z]{0,10})")) { optionName ->
        val feature =
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
          )
        val option = FeatureFlagOption(optionName, isDefault = true)

        val exception = shouldThrow<IllegalArgumentException> { Supervisor(feature, option) }

        exception shouldHaveMessage
          "Feature flag io.mehow.FeatureA does not contain option $optionName"
      }
    }
  })
