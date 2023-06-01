package io.mehow.laboratory.gradle

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
      |enum class Feature : io.mehow.laboratory.Feature<Feature> {
      |  First,
      |  Second,
      |  ;
      |
      |  public override val defaultOption: Feature
      |    get() = First
      |}
    """.trimMargin()
  }

  "generates multiple feature flags" {
    val fixture = "feature-flag-generate-multiple".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val featureA = fixture.featureFile("FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain """
      |enum class FeatureA : Feature<FeatureA> {
      |  FirstA,
      |  SecondA,
      |  ;
      |
      |  public override val defaultOption: FeatureA
      |    get() = FirstA
      |}
    """.trimMargin()

    val featureB = fixture.featureFile("FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain """
      |enum class FeatureB : Feature<FeatureB> {
      |  FirstB,
      |  SecondB,
      |  ;
      |
      |  public override val defaultOption: FeatureB
      |    get() = FirstB
      |}
    """.trimMargin()
  }

  "generates a single feature flag with source" {
    val fixture = "feature-flag-generate-sources-single".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Feature : io.mehow.laboratory.Feature<Feature> {
      |  First,
      |  Second,
      |  ;
      |
      |  public override val defaultOption: Feature
      |    get() = First
      |
      |  public override val source: Class<out io.mehow.laboratory.Feature<*>> = Source::class.java
      |
      |  public enum class Source : io.mehow.laboratory.Feature<Source> {
      |    Local,
      |    RemoteA,
      |    RemoteB,
      |    ;
      |
      |    public override val defaultOption: Source
      |      get() = Local
      |  }
      |}
    """.trimMargin()
  }

  "generates an internal feature flag with source" {
    val fixture = "feature-flag-generate-sources-internal".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |internal enum class Feature : io.mehow.laboratory.Feature<Feature> {
      |  First,
      |  Second,
      |  ;
      |
      |  public override val defaultOption: Feature
      |    get() = First
      |
      |  public override val source: Class<out io.mehow.laboratory.Feature<*>> = Source::class.java
      |
      |  internal enum class Source : io.mehow.laboratory.Feature<Source> {
      |    Local,
      |    RemoteA,
      |    RemoteB,
      |    ;
      |
      |    public override val defaultOption: Source
      |      get() = Local
      |  }
      |}
    """.trimMargin()
  }

  "generates a public feature flag with source" {
    val fixture = "feature-flag-generate-sources-public".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |public enum class Feature : io.mehow.laboratory.Feature<Feature> {
      |  First,
      |  Second,
      |  ;
      |
      |  public override val defaultOption: Feature
      |    get() = First
      |
      |  public override val source: Class<out io.mehow.laboratory.Feature<*>> = Source::class.java
      |
      |  public enum class Source : io.mehow.laboratory.Feature<Source> {
      |    Local,
      |    RemoteA,
      |    RemoteB,
      |    ;
      |
      |    public override val defaultOption: Source
      |      get() = Local
      |  }
      |}
    """.trimMargin()
  }

  "generates multiple feature flags with sources" {
    val fixture = "feature-flag-generate-sources-multiple".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val featureA = fixture.featureFile("FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain """
      |enum class FeatureA : Feature<FeatureA> {
      |  First,
      |  Second,
      |  ;
      |
      |  public override val defaultOption: FeatureA
      |    get() = First
      |
      |  public override val source: Class<out Feature<*>> = Source::class.java
      |
      |  public enum class Source : Feature<Source> {
      |    Local,
      |    RemoteA,
      |    RemoteB,
      |    ;
      |
      |    public override val defaultOption: Source
      |      get() = Local
      |  }
      |}
    """.trimMargin()

    val featureB = fixture.featureFile("FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain """
      |enum class FeatureB : Feature<FeatureB> {
      |  First,
      |  Second,
      |  ;
      |
      |  public override val defaultOption: FeatureB
      |    get() = First
      |}
    """.trimMargin()

    val featureC = fixture.featureFile("FeatureC")
    featureC.shouldExist()

    featureC.readText() shouldContain """
      |enum class FeatureC : Feature<FeatureC> {
      |  First,
      |  Second,
      |  ;
      |
      |  public override val defaultOption: FeatureC
      |    get() = First
      |
      |  public override val source: Class<out Feature<*>> = Source::class.java
      |
      |  public enum class Source : Feature<Source> {
      |    Local,
      |    RemoteA,
      |    RemoteC,
      |    ;
      |
      |    public override val defaultOption: Source
      |      get() = RemoteA
      |  }
      |}
    """.trimMargin()
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

  "uses last implicit package name for all features" {
    val fixture = "feature-flag-package-name-implicit-switching".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val featureA = fixture.featureFile("io.mehow.implicit.switch.FeatureA")
    featureA.shouldExist()

    featureA.readText() shouldContain "package io.mehow.implicit.switch"

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
      |enum class FeatureA : Feature<FeatureA> {
      |  First,
      |  Second,
      |  ;
      |
      |  public override val defaultOption: FeatureA
      |    get() = First
      |}
    """.trimMargin()

    val featureB = fixture.featureFile("FeatureB")
    featureB.shouldExist()

    featureB.readText() shouldContain """
      |enum class FeatureB : Feature<FeatureB> {
      |  First,
      |  Second,
      |  ;
      |
      |  public override val defaultOption: FeatureB
      |    get() = First
      |}
    """.trimMargin()
  }

  "fails for features with no options" {
    val fixture = "feature-flag-option-missing".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain "Feature must have at least one option"

    val feature = fixture.featureFile("Feature")
    feature.shouldNotExist()
  }

  "generates feature flag for Android project" {
    val fixture = "feature-flag-android-smoke".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Feature : io.mehow.laboratory.Feature<Feature> {
      |  First,
      |  ;
      |
      |  public override val defaultOption: Feature
      |    get() = First
      |}
    """.trimMargin()
  }

  "generates feature flag with a description" {
    val fixture = "feature-flag-generate-description".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |/**
      | * Feature description
      | */
      |public enum class Feature : io.mehow.laboratory.Feature<Feature> {
      |  First,
      |  ;
      |
      |  public override val defaultOption: Feature
      |    get() = First
      |
      |  public override val description: String = "Feature description"
      |}
    """.trimMargin()
  }

  "generates deprecated feature flag" {
    val fixture = "feature-flag-generate-deprecated".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |@Deprecated(
      |  message = "Deprecation message",
      |  level = DeprecationLevel.WARNING,
      |)
      |public enum class Feature : io.mehow.laboratory.Feature<@Suppress("DEPRECATION") Feature>
    """.trimMargin()
  }

  "generates deprecated feature flag with specified deprecation level" {
    val fixture = "feature-flag-generate-deprecated-with-level".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Feature")
    feature.shouldExist()

    feature.readText() shouldContain """
      |@Deprecated(
      |  message = "Deprecation message",
      |  level = DeprecationLevel.HIDDEN,
      |)
      |public enum class Feature : io.mehow.laboratory.Feature<@Suppress("DEPRECATION_ERROR") Feature>
    """.trimMargin()
  }

  "generates supervised child feature flag" {
    val fixture = "feature-flag-supervisor-generate-child".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Child")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Child : Feature<Child> {
      |  ChildOption,
      |  ;
      |
      |  public override val defaultOption: Child
      |    get() = ChildOption
      |
      |  public override val supervisorOption: Feature<*> = Parent.ParentOption
      |}
    """.trimMargin()
  }

  "generates supervised grandchild feature flag" {
    val fixture = "feature-flag-supervisor-generate-grandchild".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val feature = fixture.featureFile("Grandchild")
    feature.shouldExist()

    feature.readText() shouldContain """
      |enum class Grandchild : Feature<Grandchild> {
      |  GrandchildOption,
      |  ;
      |
      |  public override val defaultOption: Grandchild
      |    get() = GrandchildOption
      |
      |  public override val supervisorOption: Feature<*> = Parent.ParentOption
      |}
    """.trimMargin()
  }

  "generates supervised multiple children feature flags" {
    val fixture = "feature-flag-supervisor-generate-multiple-children".toFixture()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateFeatureFlags")!!.outcome shouldBe SUCCESS

    val first = fixture.featureFile("FirstChild")
    first.shouldExist()

    first.readText() shouldContain """
      |enum class FirstChild : Feature<FirstChild> {
      |  ChildOption,
      |  ;
      |
      |  public override val defaultOption: FirstChild
      |    get() = ChildOption
      |
      |  public override val supervisorOption: Feature<*> = Parent.FirstParentOption
      |}
    """.trimMargin()

    val second = fixture.featureFile("SecondChild")
    second.shouldExist()

    second.readText() shouldContain """
      |enum class SecondChild : Feature<SecondChild> {
      |  ChildOption,
      |  ;
      |
      |  public override val defaultOption: SecondChild
      |    get() = ChildOption
      |
      |  public override val supervisorOption: Feature<*> = Parent.SecondParentOption
      |}
    """.trimMargin()
  }

  "supervised feature flag uses explicit package name" {
    val fixture = "feature-flag-supervisor-package-name-explicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("io.mehow.explicit.Child")
    feature.shouldExist()

    feature.readText() shouldContain "package io.mehow.explicit"
  }

  "supervised feature flag uses implicit package name" {
    val fixture = "feature-flag-supervisor-package-name-explicit-override".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("io.mehow.explicit.Child")
    feature.shouldExist()

    feature.readText() shouldContain "package io.mehow.explicit"
  }

  "supervised feature flag overrides implicit package name" {
    val fixture = "feature-flag-supervisor-package-name-implicit".toFixture()

    gradleRunner.withProjectDir(fixture).build()

    val feature = fixture.featureFile("io.mehow.implicit.Child")
    feature.shouldExist()

    feature.readText() shouldContain "package io.mehow.implicit"
  }

  "fails for feature supervising itself" {
    val fixture = "feature-flag-supervisor-self-supervision".toFixture()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateFeatureFlags")!!.outcome shouldBe FAILED
    result.output shouldContain "Feature cannot supervise itself"

    val feature = fixture.featureFile("Feature")
    feature.shouldNotExist()
  }
})
