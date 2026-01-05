package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.gradle.test.LaboratoryTask
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import io.mehow.laboratory.testing.perTest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class FeatureFactoryTaskSpec : FunSpec() {
  init {
    beforeSpec { cleanBuildResults() }

    val gradleRunner by perTest { GradleRunner.create().withPluginClasspath() }

    test("registered factory") {
      val result = "feature-factory-registered".toFixture().buildFeatureFactory(gradleRunner)

      result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
      result shouldGenerateFeatureFactory "GeneratedFeatureFactory"
    }

    test("not registered factory") {
      val result = "feature-factory-not-registered".toFixture().buildFeatureFactory(gradleRunner)

      result[LaboratoryTask.FeatureFactory] shouldBeIn listOf(SUCCESS, UP_TO_DATE)
      result.shouldNotGenerateFeatureFactory()
    }

    test("implicit package name") {
      val result = "feature-factory-package-implicit".toFixture().buildFeatureFactory(gradleRunner)

      result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
      result shouldGenerateFeatureFactory "io.mehow.implicit.GeneratedFeatureFactory"
    }

    test("explicit package name") {
      val result = "feature-factory-package-explicit".toFixture().buildFeatureFactory(gradleRunner)

      result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
      result shouldGenerateFeatureFactory "io.mehow.explicit.GeneratedFeatureFactory"
    }

    test("public visibility") {
      val result = "feature-factory-visibility-public".toFixture().buildFeatureFactory(gradleRunner)

      result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
      result shouldGenerateFeatureFactory "GeneratedFeatureFactory"
    }

    test("internal visibility") {
      val result =
        "feature-factory-visibility-internal".toFixture().buildFeatureFactory(gradleRunner)

      result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
      result shouldGenerateFeatureFactory "GeneratedFeatureFactory"
    }

    test("project dependency included") {
      val result =
        "feature-factory-dependency-include".toFixture().buildFeatureFactory(gradleRunner)

      result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
      val featureFactory = result.featureFactory("GeneratedFeatureFactory")
      featureFactory shouldCreateFeature "RootFeature"
      featureFactory shouldCreateFeature "ChildFeature"
    }

    test("project dependency excluded") {
      val result =
        "feature-factory-dependency-exclude".toFixture().buildFeatureFactory(gradleRunner)

      result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
      val featureFactory = result.featureFactory("GeneratedFeatureFactory")
      featureFactory shouldCreateFeature "RootFeature"
      featureFactory shouldNotCreateFeature "ChildFeature"
    }

    test("caching") {
      val fixture = "feature-factory-registered".toFixture()
      fixture.buildFeatureFactory(gradleRunner)
      val result = fixture.buildFeatureFactory(gradleRunner)

      result[LaboratoryTask.FeatureFactory] shouldBe UP_TO_DATE
    }
  }
}
