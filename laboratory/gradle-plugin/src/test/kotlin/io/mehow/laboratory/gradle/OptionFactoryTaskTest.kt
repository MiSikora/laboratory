package io.mehow.laboratory.gradle

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.gradle.test.LaboratoryTask
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.Before
import org.junit.Test

class OptionFactoryTaskTest {
  private val gradleRunner = GradleRunner.create().withPluginClasspath()

  @Before
  fun setUp() {
    cleanBuildResults()
  }

  @Test
  fun `registered factory`() {
    val result = "option-factory-registered".toFixture().buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
    result shouldGenerateOptionFactory "GeneratedOptionFactory"
  }

  @Test
  fun `not registered factory`() {
    val result = "option-factory-not-registered".toFixture().buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBeIn listOf(SUCCESS, UP_TO_DATE)
    result.shouldNotGenerateOptionFactory()
  }

  @Test
  fun `feature keys`() {
    val result = "option-factory-key".toFixture().buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
    val optionFactory = result.optionFactory("GeneratedOptionFactory")
    optionFactory shouldCreateOptionsFor "io.mehow.FeatureA"
    optionFactory shouldCreateOptionsFor "custom key"
  }

  @Test
  fun `duplicate feature keys`() {
    val result =
      "option-factory-key-duplicate"
        .toFixture(expectFailure = true)
        .buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe FAILED
  }

  @Test
  fun `implicit package name`() {
    val result = "option-factory-package-implicit".toFixture().buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
    result shouldGenerateOptionFactory "io.mehow.implicit.GeneratedOptionFactory"
  }

  @Test
  fun `explicit package name`() {
    val result = "option-factory-package-explicit".toFixture().buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
    result shouldGenerateOptionFactory "io.mehow.explicit.GeneratedOptionFactory"
  }

  @Test
  fun `public visibility`() {
    val result = "option-factory-visibility-public".toFixture().buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
    result shouldGenerateOptionFactory "GeneratedOptionFactory"
  }

  @Test
  fun `internal visibility`() {
    val result = "option-factory-visibility-internal".toFixture().buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
    result shouldGenerateOptionFactory "GeneratedOptionFactory"
  }

  @Test
  fun `project dependency included`() {
    val result = "option-factory-dependency-include".toFixture().buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
    val optionFactory = result.optionFactory("GeneratedOptionFactory")
    optionFactory shouldCreateOptionsFor "RootFeature"
    optionFactory shouldCreateOptionsFor "ChildFeature"
  }

  @Test
  fun `project dependency excluded`() {
    val result = "option-factory-dependency-exclude".toFixture().buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
    val optionFactory = result.optionFactory("GeneratedOptionFactory")
    optionFactory shouldCreateOptionsFor "RootFeature"
    optionFactory shouldNotCreateOptionsFor "ChildFeature"
  }

  @Test
  fun `caching`() {
    val fixture = "option-factory-registered".toFixture()
    fixture.buildOptionFactory(gradleRunner)
    val result = fixture.buildOptionFactory(gradleRunner)

    result[LaboratoryTask.OptionFactory] shouldBe UP_TO_DATE
  }
}
