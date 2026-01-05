package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
  }
}
