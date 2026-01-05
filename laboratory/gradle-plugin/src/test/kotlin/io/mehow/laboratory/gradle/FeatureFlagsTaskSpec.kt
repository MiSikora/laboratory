package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.gradle.test.LaboratoryTask
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import io.mehow.laboratory.testing.perTest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class FeatureFlagsTaskSpec : FunSpec() {
  init {
    beforeSpec { cleanBuildResults() }

    val gradleRunner by perTest { GradleRunner.create().withPluginClasspath() }

    test("single feature flag") {
      val result = "feature-flags-single".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "Feature"
    }

    test("multiple feature flags") {
      val result = "feature-flags-multiple".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "FeatureA"
      result shouldGenerateFeature "FeatureB"
    }

    test("no feature flags") {
      val result = "feature-flags-none".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBeIn listOf(SUCCESS, UP_TO_DATE)
      result.shouldGenerateNoFeatures()
    }

    test("binary feature flags") {
      val result = "feature-flags-binary".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "FeatureA"
      result shouldGenerateFeature "FeatureB"
    }

    test("multiple options") {
      val result = "feature-flags-option-multiple".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "Feature"
    }

    test("duplicate options") {
      val result =
        "feature-flags-option-duplicate"
          .toFixture(expectFailure = true)
          .buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe TaskOutcome.FAILED
    }

    test("no options") {
      val result =
        "feature-flags-option-none".toFixture(expectFailure = true).buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe TaskOutcome.FAILED
    }

    test("no default option") {
      val result =
        "feature-flags-option-default-none"
          .toFixture(expectFailure = true)
          .buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe TaskOutcome.FAILED
    }

    test("multiple default options") {
      val result =
        "feature-flags-option-default-multiple"
          .toFixture(expectFailure = true)
          .buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe TaskOutcome.FAILED
    }

    test("key") {
      val result = "feature-flags-key".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "Feature"
    }

    test("package name") {
      val result = "feature-flags-package".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "io.mehow.implicit.Feature"
      result shouldGenerateFeature "io.mehow.explicit.Feature"
    }

    test("visibility") {
      val result = "feature-flags-visibility".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "PublicFeature"
      result shouldGenerateFeature "InternalFeature"
    }

    test("deprecation") {
      val result = "feature-flags-deprecation".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "WarningFeature"
      result shouldGenerateFeature "ErrorFeature"
      result shouldGenerateFeature "HiddenFeature"
    }

    test("source") {
      val result = "feature-flags-source".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "LocalFeature"
      result shouldGenerateFeature "RemoteFeature"
    }

    test("description") {
      val result = "feature-flags-description".toFixture().buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result shouldGenerateFeature "Feature"
    }

    test("caching") {
      val fixture = "feature-flags-single".toFixture()
      fixture.buildFeatureFlags(gradleRunner)
      val result = fixture.buildFeatureFlags(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe UP_TO_DATE
    }
  }
}
