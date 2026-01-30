package io.mehow.laboratory.gradle

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mehow.laboratory.gradle.test.LaboratoryTask
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.Before
import org.junit.Test

class FeatureFlagsTaskTest {
  private val gradleRunner = GradleRunner.create().withPluginClasspath()

  @Before
  fun setUp() {
    cleanBuildResults()
  }

  @Test
  fun `single feature flag`() {
    val result = "feature-flags-single".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "Feature"
  }

  @Test
  fun `multiple feature flags`() {
    val result = "feature-flags-multiple".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "FeatureA"
    result shouldGenerateFeature "FeatureB"
  }

  @Test
  fun `no feature flags`() {
    val result = "feature-flags-none".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBeIn listOf(SUCCESS, UP_TO_DATE)
    result.shouldGenerateNoFeatures()
  }

  @Test
  fun `binary feature flags`() {
    val result = "feature-flags-binary".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "FeatureA"
    result shouldGenerateFeature "FeatureB"
  }

  @Test
  fun `duplicate feature flags`() {
    val result =
      "feature-flags-duplicate".toFixture(expectFailure = true).buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe FAILED
    result.output shouldContain
      "Feature flags must have unique fully qualified names. Found following duplicates: [Feature, io.mehow.Feature]"
  }

  @Test
  fun `multiple options`() {
    val result = "feature-flags-option-multiple".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "Feature"
  }

  @Test
  fun `duplicate options`() {
    val result =
      "feature-flags-option-duplicate"
        .toFixture(expectFailure = true)
        .buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe FAILED
  }

  @Test
  fun `no options`() {
    val result =
      "feature-flags-option-none".toFixture(expectFailure = true).buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe FAILED
  }

  @Test
  fun `no default option`() {
    val result =
      "feature-flags-option-default-none"
        .toFixture(expectFailure = true)
        .buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe FAILED
  }

  @Test
  fun `multiple default options`() {
    val result =
      "feature-flags-option-default-multiple"
        .toFixture(expectFailure = true)
        .buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe FAILED
  }

  @Test
  fun `key`() {
    val result = "feature-flags-key".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "Feature"
  }

  @Test
  fun `package name`() {
    val result = "feature-flags-package".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "io.mehow.implicit.Feature"
    result shouldGenerateFeature "io.mehow.explicit.Feature"
  }

  @Test
  fun `visibility`() {
    val result = "feature-flags-visibility".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "PublicFeature"
    result shouldGenerateFeature "InternalFeature"
  }

  @Test
  fun `deprecation`() {
    val result = "feature-flags-deprecation".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "WarningFeature"
    result shouldGenerateFeature "ErrorFeature"
    result shouldGenerateFeature "HiddenFeature"
  }

  @Test
  fun `source`() {
    val result = "feature-flags-source".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "LocalFeature"
    result shouldGenerateFeature "RemoteFeature"
  }

  @Test
  fun `description`() {
    val result = "feature-flags-description".toFixture().buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result shouldGenerateFeature "Feature"
  }

  @Test
  fun `caching`() {
    val fixture = "feature-flags-single".toFixture()
    fixture.buildFeatureFlags(gradleRunner)
    val result = fixture.buildFeatureFlags(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe UP_TO_DATE
  }
}
