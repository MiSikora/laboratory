package io.mehow.laboratory.gradle

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.gradle.test.LaboratoryTask
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.Before
import org.junit.Test

class FeatureFactoryTaskTest {
  private val gradleRunner = GradleRunner.create().withPluginClasspath()

  @Before
  fun setUp() {
    cleanBuildResults()
  }

  @Test
  fun `registered factory`() {
    val result = "feature-factory-registered".toFixture().buildFeatureFactory(gradleRunner)

    result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
    result shouldGenerateFeatureFactory "GeneratedFeatureFactory"
  }

  @Test
  fun `not registered factory`() {
    val result = "feature-factory-not-registered".toFixture().buildFeatureFactory(gradleRunner)

    result[LaboratoryTask.FeatureFactory] shouldBeIn listOf(SUCCESS, UP_TO_DATE)
    result.shouldNotGenerateFeatureFactory()
  }

  @Test
  fun `implicit package name`() {
    val result = "feature-factory-package-implicit".toFixture().buildFeatureFactory(gradleRunner)

    result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
    result shouldGenerateFeatureFactory "io.mehow.implicit.GeneratedFeatureFactory"
  }

  @Test
  fun `explicit package name`() {
    val result = "feature-factory-package-explicit".toFixture().buildFeatureFactory(gradleRunner)

    result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
    result shouldGenerateFeatureFactory "io.mehow.explicit.GeneratedFeatureFactory"
  }

  @Test
  fun `public visibility`() {
    val result = "feature-factory-visibility-public".toFixture().buildFeatureFactory(gradleRunner)

    result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
    result shouldGenerateFeatureFactory "GeneratedFeatureFactory"
  }

  @Test
  fun `internal visibility`() {
    val result = "feature-factory-visibility-internal".toFixture().buildFeatureFactory(gradleRunner)

    result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
    result shouldGenerateFeatureFactory "GeneratedFeatureFactory"
  }

  @Test
  fun `project dependency included`() {
    val result = "feature-factory-dependency-include".toFixture().buildFeatureFactory(gradleRunner)

    result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
    val featureFactory = result.featureFactory("GeneratedFeatureFactory")
    featureFactory shouldCreateFeature "RootFeature"
    featureFactory shouldCreateFeature "ChildFeature"
  }

  @Test
  fun `project dependency excluded`() {
    val result = "feature-factory-dependency-exclude".toFixture().buildFeatureFactory(gradleRunner)

    result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
    val featureFactory = result.featureFactory("GeneratedFeatureFactory")
    featureFactory shouldCreateFeature "RootFeature"
    featureFactory shouldNotCreateFeature "ChildFeature"
  }

  @Test
  fun `caching`() {
    val fixture = "feature-factory-registered".toFixture()
    fixture.buildFeatureFactory(gradleRunner)
    val result = fixture.buildFeatureFactory(gradleRunner)

    result[LaboratoryTask.FeatureFactory] shouldBe UP_TO_DATE
  }
}
