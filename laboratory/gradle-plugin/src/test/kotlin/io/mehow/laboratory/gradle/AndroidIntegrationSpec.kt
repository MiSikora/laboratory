package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mehow.laboratory.gradle.test.LaboratoryTask
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import io.mehow.laboratory.testing.perTest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AndroidIntegrationSpec : FunSpec() {
  init {
    beforeSpec { cleanBuildResults() }

    val gradleRunner by perTest { GradleRunner.create().withPluginClasspath() }

    test("registered factory") {
      val result = "integration-android".toFixture().buildAll(gradleRunner)

      result[LaboratoryTask.FeatureFlags] shouldBe SUCCESS
      result[LaboratoryTask.OptionFactory] shouldBe SUCCESS
      result[LaboratoryTask.FeatureFactory] shouldBe SUCCESS
    }

    test("disabled kotlin dsl") {
      val result =
        "integration-android-no-kotlin-dsl".toFixture(expectFailure = true).buildAll(gradleRunner)

      result.output shouldContain "Laboratory Gradle plugin applied in ':' requires Kotlin plugin."
    }

    test("disabled kotlin gradle property") {
      val result =
        "integration-android-no-kotlin-gradle"
          .toFixture(expectFailure = true)
          .buildAll(gradleRunner)

      result.output shouldContain "Laboratory Gradle plugin applied in ':' requires Kotlin plugin."
    }
  }
}
