package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import io.mehow.laboratory.generator.GenerationFailure.InvalidDefaultOption
import io.mehow.laboratory.generator.GenerationFailure.MissingOption
import io.mehow.laboratory.generator.GenerationFailure.NoOption
import io.mehow.laboratory.generator.GenerationFailure.SelfSupervision
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import java.util.Locale
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.HIDDEN
import kotlin.DeprecationLevel.WARNING

internal class FeatureFlagGeneratorSpec : DescribeSpec({
  val featureBuilder = FeatureFlagModel.Builder(
      visibility = Internal,
      className = ClassName("io.mehow", "FeatureA"),
      options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
  )

  describe("feature flag model") {
    context("options") {
      it("cannot be empty") {
        val builder = featureBuilder.copy(options = emptyList())

        val result = builder.build()

        result shouldBeLeft NoOption(builder.className.canonicalName)
      }
    }

    context("default") {
      it("cannot have no options") {
        checkAll(
            Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
            Arb.stringPattern("[a-z](1)([a-z]{0,10})"),
        ) { first, second ->
          val builder = featureBuilder.copy(
              options = listOf(FeatureFlagOption(first), FeatureFlagOption(second))
          )
          val result = builder.build()

          result shouldBeLeft InvalidDefaultOption(builder.className.canonicalName, emptyList())
        }
      }

      it("cannot have multiple options") {
        checkAll(
            Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
            Arb.stringPattern("[a-z](1)([a-z]{0,10})"),
            Arb.stringPattern("[a-z](2)([a-z]{0,10})"),
        ) { first, second, third ->
          val builder = featureBuilder.copy(
              options = listOf(
                  FeatureFlagOption(first, isDefault = true),
                  FeatureFlagOption(second),
                  FeatureFlagOption(third, isDefault = true),
              )
          )
          val result = builder.build()

          result shouldBeLeft InvalidDefaultOption(builder.className.canonicalName, listOf(first, third))
        }
      }

      it("can have one option") {
        checkAll(
            Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
            Arb.stringPattern("[a-z](1)([a-z]{0,10})"),
            Arb.stringPattern("[a-z](2)([a-z]{0,10})"),
        ) { first, second, third ->
          val builder = featureBuilder.copy(
              options = listOf(
                  FeatureFlagOption(first),
                  FeatureFlagOption(second),
                  FeatureFlagOption(third, isDefault = true),
              )
          )
          val result = builder.build()

          result.shouldBeRight()
        }
      }
    }

    context("supervisor option") {
      it("cannot supervise itself") {
        val parent = featureBuilder.build().shouldBeRight()
        val supervisor = Supervisor.Builder(parent, parent.options.head).build().shouldBeRight()
        val builder = featureBuilder.copy(supervisor = supervisor)

        val result = builder.build()

        result shouldBeLeft SelfSupervision(parent.toString())
      }
    }
  }

  describe("generated feature flag") {
    it("can be internal") {
      val fileSpec = featureBuilder.build().map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |
        |internal enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  public override val defaultOption: FeatureA
        |    get() = First
        |}
        |
      """.trimMargin()
    }

    it("can be public") {
      val builder = featureBuilder.copy(visibility = Public)

      val fileSpec = builder.build().map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |
        |public enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  public override val defaultOption: FeatureA
        |    get() = First
        |}
        |
      """.trimMargin()
    }

    it("can have source parameter") {
      val fileSpec = featureBuilder
          .copy(sourceOptions = listOf(FeatureFlagOption("Remote")))
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import java.lang.Class
        |
        |internal enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  public override val defaultOption: FeatureA
        |    get() = First
        |
        |  public override val source: Class<out Feature<*>> = Source::class.java
        |
        |  internal enum class Source : Feature<Source> {
        |    Local,
        |    Remote,
        |    ;
        |
        |    public override val defaultOption: Source
        |      get() = Local
        |  }
        |}
        |
      """.trimMargin()
    }

    it("does not have source parameter if only source is Local") {
      val fileSpec = featureBuilder
          .copy(sourceOptions = listOf(FeatureFlagOption("Local")))
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |
        |internal enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  public override val defaultOption: FeatureA
        |    get() = First
        |}
        |
      """.trimMargin()
    }

    it("filters out any custom local source") {
      val localPermutations = (0b00000..0b11111).map {
        listOf(it and 0b00001, it and 0b00010, it and 0b00100, it and 0b01000, it and 0b10000)
            .map { mask -> mask != 0 }
            .mapIndexed { index, mask ->
              val chars = "local"[index].toString()
              if (mask) chars else chars.replaceFirstChar { char -> char.titlecase(Locale.ROOT) }
            }.joinToString(separator = "")
      }

      val fileSpec = featureBuilder
          .copy(sourceOptions = (localPermutations + "Remote").map(::FeatureFlagOption))
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import java.lang.Class
        |
        |internal enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  public override val defaultOption: FeatureA
        |    get() = First
        |
        |  public override val source: Class<out Feature<*>> = Source::class.java
        |
        |  internal enum class Source : Feature<Source> {
        |    Local,
        |    Remote,
        |    ;
        |
        |    public override val defaultOption: Source
        |      get() = Local
        |  }
        |}
        |
      """.trimMargin()
    }

    it("allows to set not Local default default for source") {
      val fileSpec = featureBuilder
          .copy(sourceOptions = listOf(FeatureFlagOption("Remote", isDefault = true)))
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import java.lang.Class
        |
        |internal enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  public override val defaultOption: FeatureA
        |    get() = First
        |
        |  public override val source: Class<out Feature<*>> = Source::class.java
        |
        |  internal enum class Source : Feature<Source> {
        |    Local,
        |    Remote,
        |    ;
        |
        |    public override val defaultOption: Source
        |      get() = Remote
        |  }
        |}
        |
      """.trimMargin()
    }

    it("source visibility follows feature visibility") {
      val fileSpec = featureBuilder
          .copy(visibility = Public, sourceOptions = listOf(FeatureFlagOption("Remote")))
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import java.lang.Class
        |
        |public enum class FeatureA : Feature<FeatureA> {
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
        |    Remote,
        |    ;
        |
        |    public override val defaultOption: Source
        |      get() = Local
        |  }
        |}
        |
      """.trimMargin()
    }

    context("description") {
      it("is added as KDoc") {
        val fileSpec = featureBuilder
            .copy(description = "Feature description")
            .build()
            .map { model -> model.prepare().toString() }

        fileSpec shouldBeRight """
          |package io.mehow
          |
          |import io.mehow.laboratory.Feature
          |import kotlin.String
          |
          |/**
          | * Feature description
          | */
          |internal enum class FeatureA : Feature<FeatureA> {
          |  First,
          |  Second,
          |  ;
          |
          |  public override val defaultOption: FeatureA
          |    get() = First
          |
          |  public override val description: String = "Feature description"
          |}
          |
        """.trimMargin()
      }

      it("does not break hyperlinks") {
        val fileSpec = featureBuilder
            .copy(description = "Some [long hyperlink](https://square.github.io/kotlinpoet/1.x/kotlinpoet-classinspector-elements/com.squareup.kotlinpoet.classinspector.elements/) in the KDoc.")
            .build()
            .map { model -> model.prepare().toString() }

        fileSpec shouldBeRight """
          |package io.mehow
          |
          |import io.mehow.laboratory.Feature
          |import kotlin.String
          |
          |/**
          | * Some
          | * [long hyperlink](https://square.github.io/kotlinpoet/1.x/kotlinpoet-classinspector-elements/com.squareup.kotlinpoet.classinspector.elements/)
          | * in the KDoc.
          | */
          |internal enum class FeatureA : Feature<FeatureA> {
          |  First,
          |  Second,
          |  ;
          |
          |  public override val defaultOption: FeatureA
          |    get() = First
          |
          |  public override val description: String =
          |      "Some [long hyperlink](https://square.github.io/kotlinpoet/1.x/kotlinpoet-classinspector-elements/com.squareup.kotlinpoet.classinspector.elements/) in the KDoc."
          |}
          |
        """.trimMargin()
      }
    }

    context("can be deprecated") {
      it("with warning level by default") {
        val fileSpec = featureBuilder
            .copy(deprecation = Deprecation(message = "Deprecation message"))
            .build()
            .map { model -> model.prepare().toString() }

        fileSpec shouldBeRight """
          |package io.mehow
          |
          |import io.mehow.laboratory.Feature
          |import kotlin.Deprecated
          |import kotlin.DeprecationLevel
          |import kotlin.Suppress
          |
          |@Deprecated(
          |  message = "Deprecation message",
          |  level = DeprecationLevel.WARNING
          |)
          |internal enum class FeatureA : Feature<@Suppress("DEPRECATION") FeatureA> {
          |  First,
          |  Second,
          |  ;
          |
          |  @Suppress("DEPRECATION")
          |  public override val defaultOption: FeatureA
          |    get() = First
          |}
          |
        """.trimMargin()
      }

      enumValues<DeprecationLevel>().forEach { level ->
        it("with explicit $level deprecation level") {
          val fileSpec = featureBuilder
              .copy(deprecation = Deprecation(message = "Deprecation message", level = level))
              .build()
              .map { model -> model.prepare().toString() }

          val suppressLevel = when (level) {
            WARNING -> "DEPRECATION"
            ERROR, HIDDEN -> "DEPRECATION_ERROR"
          }

          fileSpec shouldBeRight """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import kotlin.Deprecated
            |import kotlin.DeprecationLevel
            |import kotlin.Suppress
            |
            |@Deprecated(
            |  message = "Deprecation message",
            |  level = DeprecationLevel.${level}
            |)
            |internal enum class FeatureA : Feature<@Suppress("$suppressLevel") FeatureA> {
            |  First,
            |  Second,
            |  ;
            |
            |  @Suppress("$suppressLevel")
            |  public override val defaultOption: FeatureA
            |    get() = First
            |}
            |
          """.trimMargin()
        }
      }
    }

    it("can have supervisor") {
      val parent = featureBuilder.copy(className = ClassName("io.mehow.parent", "Parent")).build().shouldBeRight()
      val option = FeatureFlagOption("First")

      val fileSpec = featureBuilder
          .copy(supervisor = Supervisor.Builder(parent, option).build().shouldBeRight())
          .build()
          .map { model -> model.prepare().toString() }

      fileSpec shouldBeRight """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import io.mehow.parent.Parent
        |
        |internal enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  public override val defaultOption: FeatureA
        |    get() = First
        |
        |  public override val supervisorOption: Feature<*> = Parent.First
        |}
        |
      """.trimMargin()
    }
  }
})
