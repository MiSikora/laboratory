package io.mehow.laboratory.gradle

import io.kotest.matchers.string.shouldContain
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test

class KotlinIntegrationTest {
  private val gradleRunner = GradleRunner.create().withPluginClasspath()

  @Before
  fun setUp() {
    cleanBuildResults()
  }

  @Test
  fun `no kotlin plugin`() {
    val result =
      "integration-kotlin-no-plugin".toFixture(expectFailure = true).buildAll(gradleRunner)

    result.output shouldContain "Laboratory Gradle plugin applied in ':' requires Kotlin plugin."
  }
}
