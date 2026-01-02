package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GenerateFeatureFlagsTaskSpec :
  FunSpec({
    lateinit var gradleRunner: GradleRunner

    cleanBuildDirs()

    beforeTest {
      gradleRunner =
        GradleRunner.create()
          .withPluginClasspath()
          .withArguments("generateFeatureFlags", "--stacktrace")
    }

    test("generates single feature flag") {
      val fixture = "feature-flag-generate-single".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |enum class Feature : io.mehow.laboratory.Feature<Feature> {
        |  First,
        |  Second,
        |  ;
        |
        |  override val defaultOption: Feature
        |    get() = First
        |}
        """
          .trimMargin()
    }

    test("generates multiple feature flags") {
      val fixture = "feature-flag-generate-multiple".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val featureA = fixture.featureFile("FeatureA")
      featureA.shouldExist()

      featureA.readText() shouldContain
        """
        |enum class FeatureA : Feature<FeatureA> {
        |  FirstA,
        |  SecondA,
        |  ;
        |
        |  override val defaultOption: FeatureA
        |    get() = FirstA
        |}
        """
          .trimMargin()

      val featureB = fixture.featureFile("FeatureB")
      featureB.shouldExist()

      featureB.readText() shouldContain
        """
        |enum class FeatureB : Feature<FeatureB> {
        |  FirstB,
        |  SecondB,
        |  ;
        |
        |  override val defaultOption: FeatureB
        |    get() = FirstB
        |}
        """
          .trimMargin()
    }

    test("generates a single feature flag with source") {
      val fixture = "feature-flag-generate-sources-single".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |enum class Feature : io.mehow.laboratory.Feature<Feature> {
        |  First,
        |  Second,
        |  ;
        |
        |  override val defaultOption: Feature
        |    get() = First
        |
        |  override val source: Class<out io.mehow.laboratory.Feature<*>> = Source::class.java
        |
        |  public enum class Source : io.mehow.laboratory.Feature<Source> {
        |    Local,
        |    RemoteA,
        |    RemoteB,
        |    ;
        |
        |    override val defaultOption: Source
        |      get() = Local
        |  }
        |}
        """
          .trimMargin()
    }

    test("generates an internal feature flag with source") {
      val fixture = "feature-flag-generate-sources-internal".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |internal enum class Feature : io.mehow.laboratory.Feature<Feature> {
        |  First,
        |  Second,
        |  ;
        |
        |  override val defaultOption: Feature
        |    get() = First
        |
        |  override val source: Class<out io.mehow.laboratory.Feature<*>> = Source::class.java
        |
        |  internal enum class Source : io.mehow.laboratory.Feature<Source> {
        |    Local,
        |    RemoteA,
        |    RemoteB,
        |    ;
        |
        |    override val defaultOption: Source
        |      get() = Local
        |  }
        |}
        """
          .trimMargin()
    }

    test("generates a public feature flag with source") {
      val fixture = "feature-flag-generate-sources-public".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |public enum class Feature : io.mehow.laboratory.Feature<Feature> {
        |  First,
        |  Second,
        |  ;
        |
        |  override val defaultOption: Feature
        |    get() = First
        |
        |  override val source: Class<out io.mehow.laboratory.Feature<*>> = Source::class.java
        |
        |  public enum class Source : io.mehow.laboratory.Feature<Source> {
        |    Local,
        |    RemoteA,
        |    RemoteB,
        |    ;
        |
        |    override val defaultOption: Source
        |      get() = Local
        |  }
        |}
        """
          .trimMargin()
    }

    test("generates multiple feature flags with sources") {
      val fixture = "feature-flag-generate-sources-multiple".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val featureA = fixture.featureFile("FeatureA")
      featureA.shouldExist()

      featureA.readText() shouldContain
        """
        |enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  override val defaultOption: FeatureA
        |    get() = First
        |
        |  override val source: Class<out Feature<*>> = Source::class.java
        |
        |  public enum class Source : Feature<Source> {
        |    Local,
        |    RemoteA,
        |    RemoteB,
        |    ;
        |
        |    override val defaultOption: Source
        |      get() = Local
        |  }
        |}
        """
          .trimMargin()

      val featureB = fixture.featureFile("FeatureB")
      featureB.shouldExist()

      featureB.readText() shouldContain
        """
        |enum class FeatureB : Feature<FeatureB> {
        |  First,
        |  Second,
        |  ;
        |
        |  override val defaultOption: FeatureB
        |    get() = First
        |}
        """
          .trimMargin()

      val featureC = fixture.featureFile("FeatureC")
      featureC.shouldExist()

      featureC.readText() shouldContain
        """
        |enum class FeatureC : Feature<FeatureC> {
        |  First,
        |  Second,
        |  ;
        |
        |  override val defaultOption: FeatureC
        |    get() = First
        |
        |  override val source: Class<out Feature<*>> = Source::class.java
        |
        |  public enum class Source : Feature<Source> {
        |    Local,
        |    RemoteA,
        |    RemoteC,
        |    ;
        |
        |    override val defaultOption: Source
        |      get() = RemoteA
        |  }
        |}
        """
          .trimMargin()
    }

    test("uses implicit package name") {
      val fixture = "feature-flag-package-name-implicit".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val feature = fixture.featureFile("io.mehow.implicit.Feature")
      feature.shouldExist()

      feature.readText() shouldContain "package io.mehow.implicit"
    }

    test("cascades implicit package name") {
      val fixture = "feature-flag-package-name-implicit-cascading".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val featureA = fixture.featureFile("io.mehow.implicit.FeatureA")
      featureA.shouldExist()

      val featureB = fixture.featureFile("io.mehow.implicit.FeatureB")
      featureB.shouldExist()
    }

    test("uses last implicit package name for all features") {
      val fixture = "feature-flag-package-name-implicit-switching".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val featureA = fixture.featureFile("io.mehow.implicit.switch.FeatureA")
      featureA.shouldExist()

      featureA.readText() shouldContain "package io.mehow.implicit.switch"

      val featureB = fixture.featureFile("io.mehow.implicit.switch.FeatureB")
      featureB.shouldExist()

      featureB.readText() shouldContain "package io.mehow.implicit.switch"
    }

    test("uses explicit package name") {
      val fixture = "feature-flag-package-name-explicit".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val feature = fixture.featureFile("io.mehow.explicit.Feature")
      feature.shouldExist()

      feature.readText() shouldContain "package io.mehow.explicit"
    }

    test("switches explicit package name") {
      val fixture = "feature-flag-package-name-explicit-switching".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val featureA = fixture.featureFile("io.mehow.explicit.Feature")
      featureA.shouldExist()

      featureA.readText() shouldContain "package io.mehow.explicit"

      val featureB = fixture.featureFile("io.mehow.explicit.switch.Feature")
      featureB.shouldExist()

      featureB.readText() shouldContain "package io.mehow.explicit.switch"
    }

    test("overrides implicit package name") {
      val fixture = "feature-flag-package-name-explicit-override".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val feature = fixture.featureFile("io.mehow.explicit.Feature")
      feature.shouldExist()

      feature.readText() shouldContain "package io.mehow.explicit"
    }

    test("generates internal feature flag") {
      val fixture = "feature-flag-generate-internal".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain "internal enum class Feature"
    }

    test("generates public feature flag") {
      val fixture = "feature-flag-generate-public".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain "public enum class Feature"
    }

    test("generates features with the same options but different names") {
      val fixture = "feature-flag-generate-option-name-common".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val featureA = fixture.featureFile("FeatureA")
      featureA.shouldExist()

      featureA.readText() shouldContain
        """
        |enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  override val defaultOption: FeatureA
        |    get() = First
        |}
        """
          .trimMargin()

      val featureB = fixture.featureFile("FeatureB")
      featureB.shouldExist()

      featureB.readText() shouldContain
        """
        |enum class FeatureB : Feature<FeatureB> {
        |  First,
        |  Second,
        |  ;
        |
        |  override val defaultOption: FeatureB
        |    get() = First
        |}
        """
          .trimMargin()
    }

    test("fails for features with no options") {
      val fixture = "feature-flag-option-missing".toFixture()

      val result = gradleRunner.withProjectDir(fixture).buildAndFail()

      result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
      result.output shouldContain "Feature must have at least one option"

      val feature = fixture.featureFile("Feature")
      feature.shouldNotExist()
    }

    test("generates feature flag for Android project") {
      val fixture = "feature-flag-android-smoke".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |enum class Feature : io.mehow.laboratory.Feature<Feature> {
        |  First,
        |  ;
        |
        |  override val defaultOption: Feature
        |    get() = First
        |}
        """
          .trimMargin()
    }

    test("generates feature flag with a description") {
      val fixture = "feature-flag-generate-description".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |/**
        | * Feature description
        | */
        |public enum class Feature : io.mehow.laboratory.Feature<Feature> {
        |  First,
        |  ;
        |
        |  override val defaultOption: Feature
        |    get() = First
        |
        |  override val description: String = "Feature description"
        |}
        """
          .trimMargin()
    }

    test("generates deprecated feature flag") {
      val fixture = "feature-flag-generate-deprecated".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |@Deprecated(
        |  message = "Deprecation message",
        |  level = DeprecationLevel.WARNING,
        |)
        |public enum class Feature : io.mehow.laboratory.Feature<@Suppress("DEPRECATION") Feature>
        """
          .trimMargin()
    }

    test("generates deprecated feature flag with specified deprecation level") {
      val fixture = "feature-flag-generate-deprecated-with-level".toFixture()

      val result = gradleRunner.withProjectDir(fixture).build()

      result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |@Deprecated(
        |  message = "Deprecation message",
        |  level = DeprecationLevel.HIDDEN,
        |)
        |public enum class Feature : io.mehow.laboratory.Feature<@Suppress("DEPRECATION_ERROR") Feature>
        """
          .trimMargin()
    }

    test("generates enabled feature flag") {
      val fixture = "feature-flag-generate-enabled".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |enum class Feature : io.mehow.laboratory.Feature<Feature> {
        |  Enabled,
        |  Disabled,
        |  ;
        |
        |  override val defaultOption: Feature
        |    get() = Enabled
        |}
        """
          .trimMargin()
    }

    test("generates disabled feature flag") {
      val fixture = "feature-flag-generate-disabled".toFixture()

      gradleRunner.withProjectDir(fixture).build()

      val feature = fixture.featureFile("Feature")
      feature.shouldExist()

      feature.readText() shouldContain
        """
        |enum class Feature : io.mehow.laboratory.Feature<Feature> {
        |  Enabled,
        |  Disabled,
        |  ;
        |
        |  override val defaultOption: Feature
        |    get() = Disabled
        |}
        """
          .trimMargin()
    }
  })
