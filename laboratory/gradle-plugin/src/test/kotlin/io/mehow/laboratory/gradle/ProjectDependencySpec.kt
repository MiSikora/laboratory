package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.mehow.laboratory.gradle.test.cleanBuildResults
import io.mehow.laboratory.gradle.test.toFixture
import io.mehow.laboratory.testing.perTest
import org.gradle.testkit.runner.GradleRunner

class ProjectDependencySpec : FunSpec() {
  init {
    beforeSpec { cleanBuildResults() }

    val gradleRunner by perTest { GradleRunner.create().withPluginClasspath() }

    test("no contribution from dependency") {
      "dependency-no-contribution".toFixture(expectFailure = true).buildAll(gradleRunner)
    }

    test("no plugin in dependency") {
      "dependency-no-plugin".toFixture(expectFailure = true).buildAll(gradleRunner)
    }
  }
}
