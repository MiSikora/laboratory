package io.mehow.laboratory.gradle

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mehow.laboratory.gradle.test.LaboratoryTask
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Before
import org.junit.Test

class AndroidIntegrationTest {
  private val gradleRunner = GradleRunner.create().withPluginClasspath()

  @Before
  fun setUp() {
    cleanBuildResults()
  }

  @Test
  fun `registered factory`() {
    val result = "integration-android".toFixture().buildAll(gradleRunner)

    result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
    result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
    result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
  }

  @Test
  fun `disabled kotlin dsl`() {
    val result =
      "integration-android-no-kotlin-dsl".toFixture(expectFailure = true).buildAll(gradleRunner)

    result.output shouldContain "Laboratory Gradle plugin applied in ':' requires Kotlin plugin."
  }

  @Test
  fun `disabled kotlin gradle property`() {
    val result =
      "integration-android-no-kotlin-gradle".toFixture(expectFailure = true).buildAll(gradleRunner)

    result.output shouldContain "Laboratory Gradle plugin applied in ':' requires Kotlin plugin."
  }
}
