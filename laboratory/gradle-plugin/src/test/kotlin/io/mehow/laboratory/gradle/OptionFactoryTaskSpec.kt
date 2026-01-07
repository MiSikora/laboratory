package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.gradle.test.LaboratoryTask
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import io.mehow.laboratory.testing.perTest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class OptionFactoryTaskSpec : FunSpec() {
  init {
    beforeSpec { cleanBuildResults() }

    val gradleRunner by perTest { GradleRunner.create().withPluginClasspath() }

    test("registered factory") {
      val result = "option-factory-registered".toFixture().buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
      result shouldGenerateOptionFactory "GeneratedOptionFactory"
    }

    test("not registered factory") {
      val result = "option-factory-not-registered".toFixture().buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBeIn listOf(SUCCESS, UP_TO_DATE)
      result.shouldNotGenerateOptionFactory()
    }

    test("feature keys") {
      val result = "option-factory-key".toFixture().buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
      val optionFactory = result.optionFactory("GeneratedOptionFactory")
      optionFactory shouldCreateOptionsFor "io.mehow.FeatureA"
      optionFactory shouldCreateOptionsFor "custom key"
    }

    test("duplicate feature keys") {
      val result =
        "option-factory-key-duplicate"
          .toFixture(expectFailure = true)
          .buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe FAILED
    }

    test("implicit package name") {
      val result = "option-factory-package-implicit".toFixture().buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
      result shouldGenerateOptionFactory "io.mehow.implicit.GeneratedOptionFactory"
    }

    test("explicit package name") {
      val result = "option-factory-package-explicit".toFixture().buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
      result shouldGenerateOptionFactory "io.mehow.explicit.GeneratedOptionFactory"
    }

    test("public visibility") {
      val result = "option-factory-visibility-public".toFixture().buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
      result shouldGenerateOptionFactory "GeneratedOptionFactory"
    }

    test("internal visibility") {
      val result = "option-factory-visibility-internal".toFixture().buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
      result shouldGenerateOptionFactory "GeneratedOptionFactory"
    }

    test("project dependency included") {
      val result = "option-factory-dependency-include".toFixture().buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
      val optionFactory = result.optionFactory("GeneratedOptionFactory")
      optionFactory shouldCreateOptionsFor "RootFeature"
      optionFactory shouldCreateOptionsFor "ChildFeature"
    }

    test("project dependency excluded") {
      val result = "option-factory-dependency-exclude".toFixture().buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
      val optionFactory = result.optionFactory("GeneratedOptionFactory")
      optionFactory shouldCreateOptionsFor "RootFeature"
      optionFactory shouldNotCreateOptionsFor "ChildFeature"
    }

    test("caching") {
      val fixture = "option-factory-registered".toFixture()
      fixture.buildOptionFactory(gradleRunner)
      val result = fixture.buildOptionFactory(gradleRunner)

      result[LaboratoryTask.OptionFactory] shouldBe UP_TO_DATE
    }
  }
}
