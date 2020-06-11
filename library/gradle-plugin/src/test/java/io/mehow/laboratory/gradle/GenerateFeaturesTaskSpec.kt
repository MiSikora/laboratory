package io.mehow.laboratory.gradle

import arrow.core.Nel
import arrow.core.nel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mehow.laboratory.generator.FeatureValuesCollision
import io.mehow.laboratory.generator.FeaturesCollision
import io.mehow.laboratory.generator.InvalidFeatureName
import io.mehow.laboratory.generator.InvalidFeatureValues
import io.mehow.laboratory.generator.InvalidPackageName
import io.mehow.laboratory.generator.NoFeatureValues
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import java.io.File

class GenerateFeaturesTaskSpec : StringSpec({
  lateinit var gradleRunner: GradleRunner

  beforeTest {
    gradleRunner = GradleRunner.create()
      .withPluginClasspath()
      .withArguments("generateFeatureFlags", "--stacktrace")
  }

  afterTest {
    File("src/test/projects").getOutputDirs().forEach(File::cleanUpDir)
  }

  "generates single feature flag" {
    val fixture = "feature-generate-single".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Feature {
      |  First,
      |
      |  Second
      |}
    """.trimMargin("|")
  }

  "generates multiple feature flags" {
    val fixture = "feature-generate-multiple".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val featureA = fixture.featureFile("FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain """
      |enum class FeatureA {
      |  FirstA,
      |
      |  SecondA
      |}
    """.trimMargin("|")

    val featureB = fixture.featureFile("FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain """
      |enum class FeatureB {
      |  FirstB,
      |
      |  SecondB
      |}
    """.trimMargin("|")
  }

  "uses implicit package name" {
    val fixture = "feature-package-name-implicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("io.mehow.implicit.Feature")
    feature.shouldExist()

    feature.readText() shouldContain "package io.mehow.implicit"
  }

  "cascades implicit package name" {
    val fixture = "feature-package-name-implicit-cascading".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val featureA = fixture.featureFile("io.mehow.implicit.FeatureA")
    featureA.shouldExist()

    val featureB = fixture.featureFile("io.mehow.implicit.FeatureB")
    featureB.shouldExist()
  }

  "switches implicit package name" {
    val fixture = "feature-package-name-implicit-switching".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val featureA = fixture.featureFile("io.mehow.implicit.FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain "package io.mehow.implicit"

    val featureB = fixture.featureFile("io.mehow.implicit.switch.FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain "package io.mehow.implicit.switch"
  }

  "uses explicit package name" {
    val fixture = "feature-package-name-explicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("io.mehow.explicit.Feature")
    feature.shouldExist()

    feature.readText() shouldContain "package io.mehow.explicit"
  }

  "switches explicit package name" {
    val fixture = "feature-package-name-explicit-switching".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val featureA = fixture.featureFile("io.mehow.explicit.Feature")
    featureA.shouldExist()

    featureA.readText() shouldContain "package io.mehow.explicit"

    val featureB = fixture.featureFile("io.mehow.explicit.switch.Feature")
    featureB.shouldExist()

    featureB.readText() shouldContain "package io.mehow.explicit.switch"
  }

  "overrides implicit package name" {
    val fixture = "feature-package-name-explicit-override".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("io.mehow.explicit.Feature")
    feature.shouldExist()

    feature.readText() shouldContain "package io.mehow.explicit"
  }

  "generates internal feature flag" {
    val fixture = "feature-generate-internal".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain "internal enum class Feature"
  }

  "generates public feature flag" {
    val fixture = "feature-generate-public".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    // Ensure public by checking a new line before enum declaration.
    // Change after https://github.com/square/kotlinpoet/pull/933
    feature.readText() shouldContain """
      |
      |enum class Feature
    """.trimMargin("|")
  }

  "generates features with same values but different names" {
    val fixture = "feature-generate-common-values".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val featureA = fixture.featureFile("FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain """
      |enum class FeatureA {
      |  First,
      |
      |  Second
      |}
    """.trimMargin("|")

    val featureB = fixture.featureFile("FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain """
      |enum class FeatureB {
      |  First,
      |
      |  Second
      |}
    """.trimMargin("|")
  }

  "fails for features with no values" {
    val fixture = "feature-values-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain NoFeatureValues("Feature").message

    val feature = fixture.featureFile("Feature")
    feature.shouldNotExist()
  }

  "fails for features with colliding values" {
    val fixture = "feature-values-colliding".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain FeatureValuesCollision("First".nel(), "Feature").message

    val feature = fixture.featureFile("Feature")
    feature.shouldNotExist()
  }

  "fails for features with corrupted values" {
    val fixture = "feature-values-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidFeatureValues(Nel("!!!, ???"), "Feature").message

    val feature = fixture.featureFile("Feature")
    feature.shouldNotExist()
  }

  "fails for features with corrupted names" {
    val fixture = "feature-name-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidFeatureName("!!!", "!!!").message

    val feature = fixture.featureFile("!!!")
    feature.shouldNotExist()
  }

  "fails for features with corrupted package names" {
    val fixture = "feature-package-name-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidPackageName("!!!.Feature").message

    val feature = fixture.featureFile("!!!.Feature")
    feature.shouldNotExist()
  }

  "fails for features with colliding namespaces" {
    val fixture = "feature-namespace-colliding".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain FeaturesCollision("io.mehow.Feature".nel()).message

    val feature = fixture.featureFile("io.mehow.Feature")
    feature.shouldNotExist()
  }

  "generates feature flag for Android project" {
    val fixture = "feature-android-smoke".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Feature {
      |  First
      |}
    """.trimMargin("|")
  }
})
