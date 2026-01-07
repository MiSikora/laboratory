package io.mehow.laboratory.gradle.test

import io.kotest.assertions.withClue
import io.kotest.matchers.file.shouldBeEmptyDirectory
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File
import org.gradle.testkit.runner.BuildResult as GradleBuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

fun cleanBuildResults() = File("src/test/projects").getBuildDirs().forEach(File::deleteRecursively)

private fun File.getBuildDirs(): List<File> =
  when (name) {
    "build" -> listOf(this)
    else -> listFiles().orEmpty().flatMap(File::getBuildDirs)
  }

enum class LaboratoryTask(val taskName: String, val codeGenDir: String) {
  FeatureFlags(taskName = "generateFeatureFlags", codeGenDir = "feature-flags"),
  FeatureFactory(taskName = "generateFeatureFactory", codeGenDir = "feature-factory"),
  OptionFactory(taskName = "generateOptionFactory", codeGenDir = "option-factory"),
}

fun String.toFixture(expectFailure: Boolean = false) =
  Fixture(File("src/test/projects/$this"), expectFailure)

class Fixture(private val projectDir: File, private val expectFailure: Boolean) {
  fun buildFeatureFlags(gradleRunner: GradleRunner) =
    build(gradleRunner, LaboratoryTask.FeatureFlags)

  fun buildFeatureFactory(gradleRunner: GradleRunner) =
    build(gradleRunner, LaboratoryTask.FeatureFactory)

  fun buildOptionFactory(gradleRunner: GradleRunner) =
    build(gradleRunner, LaboratoryTask.OptionFactory)

  fun buildAll(gradleRunner: GradleRunner) = build(gradleRunner, LaboratoryTask.entries)

  fun build(gradleRunner: GradleRunner, task: LaboratoryTask, vararg tasks: LaboratoryTask) =
    build(gradleRunner, tasks.toList() + task)

  private fun build(gradleRunner: GradleRunner, tasks: List<LaboratoryTask>): BuildResult {
    val runnerArgs = tasks.map(LaboratoryTask::taskName) + "--stacktrace"
    val projectRunner = gradleRunner.withProjectDir(projectDir).withArguments(runnerArgs)
    val buildResult = if (expectFailure) projectRunner.buildAndFail() else projectRunner.build()
    return BuildResult(projectDir, buildResult)
  }
}

class BuildResult(private val projectDir: File, private val result: GradleBuildResult) {
  val output
    get() = result.output

  operator fun get(task: LaboratoryTask): TaskOutcome {
    val task =
      withClue("Expected '${projectDir.name}' project to have ${task.taskName} task") {
        result.task(":${task.taskName}").shouldNotBeNull()
      }
    return task.outcome
  }

  infix fun shouldGenerateFeature(fqcn: String) {
    withClue("Expected '${projectDir.name}' project to generate '$fqcn' feature") {
      codeGenFile(LaboratoryTask.FeatureFlags, fqcn).shouldExist()
    }
  }

  fun shouldGenerateNoFeatures() {
    val dir = codeGenDir(LaboratoryTask.FeatureFlags)
    if (dir.exists()) {
      withClue("Expected '${projectDir.name}' project to generate no features") {
        dir.shouldBeEmptyDirectory()
      }
    }
  }

  infix fun shouldGenerateFeatureFactory(fqcn: String) {
    withClue("Expected '${projectDir.name}' project to generate '$fqcn' feature factory") {
      codeGenFile(LaboratoryTask.FeatureFactory, fqcn).shouldExist()
    }
  }

  fun shouldNotGenerateFeatureFactory() {
    val dir = codeGenDir(LaboratoryTask.FeatureFactory)
    if (dir.exists()) {
      withClue("Expected '${projectDir.name}' project to generate no feature factory") {
        dir.shouldBeEmptyDirectory()
      }
    }
  }

  fun featureFactory(fqcn: String): GeneratedFeatureFactory {
    shouldGenerateFeatureFactory(fqcn)
    val code = codeGenFile(LaboratoryTask.FeatureFactory, fqcn).readText()
    return GeneratedFeatureFactory(projectDir.name, fqcn, code)
  }

  infix fun shouldGenerateOptionFactory(fqcn: String) {
    withClue("Expected '${projectDir.name}' project to generate '$fqcn' option factory") {
      codeGenFile(LaboratoryTask.OptionFactory, fqcn).shouldExist()
    }
  }

  fun shouldNotGenerateOptionFactory() {
    val dir = codeGenDir(LaboratoryTask.OptionFactory)
    if (dir.exists()) {
      withClue("Expected '${projectDir.name}' project to generate no option factory") {
        dir.shouldBeEmptyDirectory()
      }
    }
  }

  fun optionFactory(fqcn: String): GeneratedOptionFactory {
    shouldGenerateOptionFactory(fqcn)
    val code = codeGenFile(LaboratoryTask.OptionFactory, fqcn).readText()
    return GeneratedOptionFactory(projectDir.name, fqcn, code)
  }

  private fun codeGenFile(task: LaboratoryTask, fqcn: String) =
    File(codeGenDir(task), "${fqcn.replace(".", "/")}.kt")

  private fun codeGenDir(task: LaboratoryTask) =
    File(projectDir, "build/generated/laboratory/code/${task.codeGenDir}/")
}

class GeneratedOptionFactory(
  private val projectName: String,
  private val fqcn: String,
  private val code: String,
) {
  infix fun shouldCreateOptionsFor(featureKey: String) {
    withClue(
      "Expected '$projectName' to generate option factory '$fqcn' with options for feature '$featureKey'"
    ) {
      code shouldContain "\"$featureKey\" -> when"
    }
  }

  infix fun shouldNotCreateOptionsFor(featureKey: String) {
    withClue(
      "Expected '$projectName' to not generate option factory '$fqcn' with options for feature '$featureKey'"
    ) {
      code shouldNotContain "\"$featureKey\" -> when"
    }
  }
}

class GeneratedFeatureFactory(
  private val projectName: String,
  private val fqcn: String,
  private val code: String,
) {
  infix fun shouldCreateFeature(featureFqcn: String) {
    withClue(
      "Expected '$projectName' to generate feature factory '$fqcn' with feature '$featureFqcn'"
    ) {
      code shouldContain "Class.forName(\"$featureFqcn\")"
    }
  }

  infix fun shouldNotCreateFeature(featureFqcn: String) {
    withClue(
      "Expected '$projectName' to not generate feature factory '$fqcn' with feature '$featureFqcn'"
    ) {
      code shouldNotContain "Class.forName(\"$featureFqcn\")"
    }
  }
}
