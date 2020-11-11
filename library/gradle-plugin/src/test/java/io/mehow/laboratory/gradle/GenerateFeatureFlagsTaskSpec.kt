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

internal class GenerateFeatureFlagsTaskSpec : StringSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create()
            .withPluginClasspath()
            .withArguments("generateFeatureFlags", "--stacktrace")
  }

  "generates single feature flag" {
    val fixture = "feature-flag-generate-single".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Feature(
      |  public override val isDefaultValue: Boolean = false
      |) : io.mehow.laboratory.Feature<Feature> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |}
    """.trimMargin("|")
  }

  "generates multiple feature flags" {
    val fixture = "feature-flag-generate-multiple".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val featureA = fixture.featureFile("FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain """
      |enum class FeatureA(
      |  public override val isDefaultValue: Boolean = false
      |) : Feature<FeatureA> {
      |  FirstA(isDefaultValue = true),
      |  SecondA,
      |  ;
      |}
    """.trimMargin("|")

    val featureB = fixture.featureFile("FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain """
      |enum class FeatureB(
      |  public override val isDefaultValue: Boolean = false
      |) : Feature<FeatureB> {
      |  FirstB(isDefaultValue = true),
      |  SecondB,
      |  ;
      |}
    """.trimMargin("|")
  }

  "generates a single feature flag with source" {
    val fixture = "feature-flag-generate-sources-single".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Feature(
      |  public override val isDefaultValue: Boolean = false
      |) : io.mehow.laboratory.Feature<Feature> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |
      |  @Suppress("UNCHECKED_CAST")
      |  public override val source: Class<io.mehow.laboratory.Feature<*>> = Source::class.java as
      |      Class<io.mehow.laboratory.Feature<*>>
      |
      |  public enum class Source(
      |    public override val isDefaultValue: Boolean = false
      |  ) : io.mehow.laboratory.Feature<Source> {
      |    Local(isDefaultValue = true),
      |    RemoteA,
      |    RemoteB,
      |    ;
      |  }
      |}
    """.trimMargin("|")
  }

  "generates an internal feature flag with source" {
    val fixture = "feature-flag-generate-sources-internal".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |internal enum class Feature(
      |  public override val isDefaultValue: Boolean = false
      |) : io.mehow.laboratory.Feature<Feature> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |
      |  @Suppress("UNCHECKED_CAST")
      |  public override val source: Class<io.mehow.laboratory.Feature<*>> = Source::class.java as
      |      Class<io.mehow.laboratory.Feature<*>>
      |
      |  internal enum class Source(
      |    public override val isDefaultValue: Boolean = false
      |  ) : io.mehow.laboratory.Feature<Source> {
      |    Local(isDefaultValue = true),
      |    RemoteA,
      |    RemoteB,
      |    ;
      |  }
      |}
    """.trimMargin("|")
  }

  "generates a public feature flag with source" {
    val fixture = "feature-flag-generate-sources-public".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |public enum class Feature(
      |  public override val isDefaultValue: Boolean = false
      |) : io.mehow.laboratory.Feature<Feature> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |
      |  @Suppress("UNCHECKED_CAST")
      |  public override val source: Class<io.mehow.laboratory.Feature<*>> = Source::class.java as
      |      Class<io.mehow.laboratory.Feature<*>>
      |
      |  public enum class Source(
      |    public override val isDefaultValue: Boolean = false
      |  ) : io.mehow.laboratory.Feature<Source> {
      |    Local(isDefaultValue = true),
      |    RemoteA,
      |    RemoteB,
      |    ;
      |  }
      |}
    """.trimMargin("|")
  }

  "generates multiple feature flags with sources" {
    val fixture = "feature-flag-generate-sources-multiple".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val featureA = fixture.featureFile("FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain """
      |enum class FeatureA(
      |  public override val isDefaultValue: Boolean = false
      |) : Feature<FeatureA> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |
      |  @Suppress("UNCHECKED_CAST")
      |  public override val source: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
      |
      |  public enum class Source(
      |    public override val isDefaultValue: Boolean = false
      |  ) : Feature<Source> {
      |    Local(isDefaultValue = true),
      |    RemoteA,
      |    RemoteB,
      |    ;
      |  }
      |}
    """.trimMargin("|")

    val featureB = fixture.featureFile("FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain """
      |enum class FeatureB(
      |  public override val isDefaultValue: Boolean = false
      |) : Feature<FeatureB> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |}
    """.trimMargin("|")

    val featureC = fixture.featureFile("FeatureC")
    featureC.shouldExist()

    featureC.readText() shouldContain """
      |enum class FeatureC(
      |  public override val isDefaultValue: Boolean = false
      |) : Feature<FeatureC> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |
      |  @Suppress("UNCHECKED_CAST")
      |  public override val source: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
      |
      |  public enum class Source(
      |    public override val isDefaultValue: Boolean = false
      |  ) : Feature<Source> {
      |    Local,
      |    RemoteA(isDefaultValue = true),
      |    RemoteC,
      |    ;
      |  }
      |}
    """.trimMargin("|")
  }

  "uses implicit package name" {
    val fixture = "feature-flag-package-name-implicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("io.mehow.implicit.Feature")
    feature.shouldExist()

    feature.readText() shouldContain "package io.mehow.implicit"
  }

  "cascades implicit package name" {
    val fixture = "feature-flag-package-name-implicit-cascading".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val featureA = fixture.featureFile("io.mehow.implicit.FeatureA")
    featureA.shouldExist()

    val featureB = fixture.featureFile("io.mehow.implicit.FeatureB")
    featureB.shouldExist()
  }

  "switches implicit package name" {
    val fixture = "feature-flag-package-name-implicit-switching".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val featureA = fixture.featureFile("io.mehow.implicit.FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain "package io.mehow.implicit"

    val featureB = fixture.featureFile("io.mehow.implicit.switch.FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain "package io.mehow.implicit.switch"
  }

  "uses explicit package name" {
    val fixture = "feature-flag-package-name-explicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("io.mehow.explicit.Feature")
    feature.shouldExist()

    feature.readText() shouldContain "package io.mehow.explicit"
  }

  "switches explicit package name" {
    val fixture = "feature-flag-package-name-explicit-switching".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val featureA = fixture.featureFile("io.mehow.explicit.Feature")
    featureA.shouldExist()

    featureA.readText() shouldContain "package io.mehow.explicit"

    val featureB = fixture.featureFile("io.mehow.explicit.switch.Feature")
    featureB.shouldExist()

    featureB.readText() shouldContain "package io.mehow.explicit.switch"
  }

  "overrides implicit package name" {
    val fixture = "feature-flag-package-name-explicit-override".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("io.mehow.explicit.Feature")
    feature.shouldExist()

    feature.readText() shouldContain "package io.mehow.explicit"
  }

  "generates internal feature flag" {
    val fixture = "feature-flag-generate-internal".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain "internal enum class Feature"
  }

  "generates public feature flag" {
    val fixture = "feature-flag-generate-public".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain "public enum class Feature"
  }

  "generates features with the same options but different names" {
    val fixture = "feature-flag-generate-option-name-common".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val featureA = fixture.featureFile("FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain """
      |enum class FeatureA(
      |  public override val isDefaultValue: Boolean = false
      |) : Feature<FeatureA> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |}
    """.trimMargin("|")

    val featureB = fixture.featureFile("FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain """
      |enum class FeatureB(
      |  public override val isDefaultValue: Boolean = false
      |) : Feature<FeatureB> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |}
    """.trimMargin("|")
  }

  "fails for features with no options" {
    val fixture = "feature-flag-option-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain NoFeatureValues("Feature").message

    val feature = fixture.featureFile("Feature")
    feature.shouldNotExist()
  }

  "fails for features with colliding options" {
    val fixture = "feature-flag-option-colliding".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain FeatureValuesCollision("First".nel(), "Feature").message

    val feature = fixture.featureFile("Feature")
    feature.shouldNotExist()
  }

  "fails for features with corrupted options" {
    val fixture = "feature-flag-option-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidFeatureValues(Nel("!!!, ???"), "Feature").message

    val feature = fixture.featureFile("Feature")
    feature.shouldNotExist()
  }

  "fails for features with corrupted names" {
    val fixture = "feature-flag-name-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidFeatureName("!!!", "!!!").message

    val feature = fixture.featureFile("!!!")
    feature.shouldNotExist()
  }

  "fails for features with corrupted package names" {
    val fixture = "feature-flag-package-name-corrupted".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain InvalidPackageName("!!!.Feature").message

    val feature = fixture.featureFile("!!!.Feature")
    feature.shouldNotExist()
  }

  "fails for features with colliding namespaces" {
    val fixture = "feature-flag-namespace-colliding".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain FeaturesCollision("io.mehow.Feature".nel()).message

    val feature = fixture.featureFile("io.mehow.Feature")
    feature.shouldNotExist()
  }

  "generates feature flag for Android project" {
    val fixture = "feature-flag-android-smoke".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Feature(
      |  public override val isDefaultValue: Boolean = false
      |) : io.mehow.laboratory.Feature<Feature> {
      |  First(isDefaultValue = true),
      |  ;
      |}
    """.trimMargin("|")
  }

  "generates feature flag with a description" {
    val fixture = "feature-flag-generate-description".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Feature(
      |  public override val isDefaultValue: Boolean = false
      |) : io.mehow.laboratory.Feature<Feature> {
      |  First(isDefaultValue = true),
      |  ;
      |
      |  public override val description: String = "Feature description"
      |}
    """.trimMargin("|")
  }

  "generates feature flag from Groovy DSL" {
    val fixture = "feature-flag-generate-groovy-dsl".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("io.mehow.example.Feature")
    feature.shouldExist()

    feature.readText() shouldBe """
      |package io.mehow.example
      |
      |import java.lang.Class
      |import kotlin.Boolean
      |import kotlin.String
      |import kotlin.Suppress
      |
      |public enum class Feature(
      |  public override val isDefaultValue: Boolean = false
      |) : io.mehow.laboratory.Feature<Feature> {
      |  First(isDefaultValue = true),
      |  Second,
      |  ;
      |
      |  @Suppress("UNCHECKED_CAST")
      |  public override val source: Class<io.mehow.laboratory.Feature<*>> = Source::class.java as
      |      Class<io.mehow.laboratory.Feature<*>>
      |
      |  public override val description: String = "Feature description"
      |
      |  public enum class Source(
      |    public override val isDefaultValue: Boolean = false
      |  ) : io.mehow.laboratory.Feature<Source> {
      |    Local(isDefaultValue = true),
      |    Remote,
      |    ;
      |  }
      |}
      |
    """.trimMargin("|")
  }
})
