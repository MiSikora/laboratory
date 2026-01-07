package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import io.mehow.laboratory.testing.perTest
import org.gradle.testkit.runner.GradleRunner

class KotlinIntegrationSpec : FunSpec() {
  init {
    beforeSpec { cleanBuildResults() }

    val gradleRunner by perTest { GradleRunner.create().withPluginClasspath() }

    test("no kotlin plugin") {
      val result =
        "integration-kotlin-no-plugin".toFixture(expectFailure = true).buildAll(gradleRunner)

      result.output shouldContain "Laboratory Gradle plugin applied in ':' requires Kotlin plugin."
    }
  }
}
