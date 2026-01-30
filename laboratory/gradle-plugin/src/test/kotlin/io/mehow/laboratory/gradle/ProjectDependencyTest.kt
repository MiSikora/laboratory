package io.mehow.laboratory.gradle

import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test

class ProjectDependencyTest {
  private val gradleRunner = GradleRunner.create().withPluginClasspath()

  @Before
  fun setUp() {
    cleanBuildResults()
  }

  @Test
  fun `no contribution from dependency`() {
    "dependency-no-contribution".toFixture(expectFailure = true).buildAll(gradleRunner)
  }

  @Test
  fun `no plugin in dependency`() {
    "dependency-no-plugin".toFixture(expectFailure = true).buildAll(gradleRunner)
  }
}
