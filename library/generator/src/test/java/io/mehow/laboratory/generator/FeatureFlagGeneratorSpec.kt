package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.property.Arb
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.generator.test.shouldSpecify
import java.util.Locale
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.HIDDEN
import kotlin.DeprecationLevel.WARNING

internal class FeatureFlagGeneratorSpec : DescribeSpec({
  describe("feature flag model") {
    context("options") {
      it("cannot be empty") {
        val exception = shouldThrow<IllegalArgumentException> {
          FeatureFlagModel(
              ClassName("io.mehow", "FeatureA"),
              options = emptyList(),
          )
        }

        exception shouldHaveMessage "io.mehow.FeatureA must have at least one option"
      }
    }

    context("default") {
      it("cannot have no options") {
        checkAll(
            Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
            Arb.stringPattern("[a-z](1)([a-z]{0,10})"),
        ) { first, second ->
          val exception = shouldThrow<IllegalArgumentException> {
            FeatureFlagModel(
                ClassName("io.mehow", "FeatureA"),
                listOf(FeatureFlagOption(first), FeatureFlagOption(second)),
            )
          }

          exception shouldHaveMessage "io.mehow.FeatureA must have exactly one default option"
        }
      }

      it("cannot have multiple options") {
        checkAll(
            Arb.stringPattern("[a-z](0)([a-z]{0,10})"),
            Arb.stringPattern("[a-z](1)([a-z]{0,10})"),
            Arb.stringPattern("[a-z](2)([a-z]{0,10})"),
        ) { first, second, third ->
          val exception = shouldThrow<IllegalArgumentException> {
            FeatureFlagModel(
                ClassName("io.mehow", "FeatureA"),
                listOf(
                    FeatureFlagOption(first, isDefault = true),
                    FeatureFlagOption(second),
                    FeatureFlagOption(third, isDefault = true),
                ),
            )
          }

          exception shouldHaveMessage "io.mehow.FeatureA must have exactly one default option"
        }
      }
    }

    context("supervisor option") {
      it("cannot supervise itself") {
        val model = FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("First", isDefault = true)),
        )

        val exception = shouldThrow<IllegalArgumentException> {
          FeatureFlagModel(
              model.className,
              model.options,
              supervisor = Supervisor(model, model.options.first()),
          )
        }

        exception shouldHaveMessage "io.mehow.FeatureA cannot supervise itself"
      }
    }
  }

  describe("generated feature flag") {
    it("can be internal") {
      val model = FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
          visibility = Internal,
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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
      val model = FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
          visibility = Public,
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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

    it("can have single option") {
      val model = FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("First", isDefault = true)),
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |
        |public enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  ;
        |
        |  public override val defaultOption: FeatureA
        |    get() = First
        |}
        |
      """.trimMargin()
    }

    it("can have source") {
      val model = FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
          sourceOptions = listOf(FeatureFlagOption("Remote")),
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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

    it("does not have source parameter if only source is Local") {
      val model = FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
          sourceOptions = listOf(FeatureFlagOption("Local")),
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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

    it("filters out any custom local source") {
      val localPermutations = (0b00000..0b11111).map {
        listOf(it and 0b00001, it and 0b00010, it and 0b00100, it and 0b01000, it and 0b10000)
            .map { mask -> mask != 0 }
            .mapIndexed { index, mask ->
              val chars = "local"[index].toString()
              if (mask) chars else chars.replaceFirstChar { char -> char.titlecase(Locale.ROOT) }
            }.joinToString(separator = "")
      }
      val model = FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
          sourceOptions = (localPermutations + "Remote").map(::FeatureFlagOption),
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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

    it("can change default source") {
      val model = FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
          sourceOptions = listOf(FeatureFlagOption("Remote", isDefault = true)),
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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
        |      get() = Remote
        |  }
        |}
        |
      """.trimMargin()
    }

    it("source visibility follows feature visibility") {
      val model = FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
          visibility = Internal,
          sourceOptions = listOf(FeatureFlagOption("Remote")),
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
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

    context("description") {
      it("is added as KDoc") {
        val model = FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
            description = "Feature description",
        )

        val fileSpec = model.prepare()

        fileSpec shouldSpecify """
          |package io.mehow
          |
          |import io.mehow.laboratory.Feature
          |import kotlin.String
          |
          |/**
          | * Feature description
          | */
          |public enum class FeatureA : Feature<FeatureA> {
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
        val model = FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
            description = "Some [long hyperlink](https://square.github.io/kotlinpoet/1.x/kotlinpoet-classinspector-elements/com.squareup.kotlinpoet.classinspector.elements/) in the KDoc.",
        )

        val fileSpec = model.prepare()

        fileSpec shouldSpecify """
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
          |public enum class FeatureA : Feature<FeatureA> {
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
        val model = FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
            deprecation = Deprecation("Deprecation message"),
        )

        val fileSpec = model.prepare()

        fileSpec shouldSpecify """
          |package io.mehow
          |
          |import io.mehow.laboratory.Feature
          |import kotlin.Deprecated
          |import kotlin.DeprecationLevel
          |import kotlin.Suppress
          |
          |@Deprecated(
          |  message = "Deprecation message",
          |  level = DeprecationLevel.WARNING,
          |)
          |public enum class FeatureA : Feature<@Suppress("DEPRECATION") FeatureA> {
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
          val model = FeatureFlagModel(
              ClassName("io.mehow", "FeatureA"),
              listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
              deprecation = Deprecation("Deprecation message", level),
          )
          val suppressLevel = when (level) {
            WARNING -> "DEPRECATION"
            ERROR, HIDDEN -> "DEPRECATION_ERROR"
          }

          val fileSpec = model.prepare()

          fileSpec shouldSpecify """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import kotlin.Deprecated
            |import kotlin.DeprecationLevel
            |import kotlin.Suppress
            |
            |@Deprecated(
            |  message = "Deprecation message",
            |  level = DeprecationLevel.${level},
            |)
            |public enum class FeatureA : Feature<@Suppress("$suppressLevel") FeatureA> {
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
      val supervisor = FeatureFlagModel(
          ClassName("io.mehow.supervisor", "Supervisor"),
          listOf(FeatureFlagOption("First", isDefault = true)),
      )
      val model = FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
          supervisor = Supervisor(supervisor, supervisor.options.first()),
      )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify """
        |package io.mehow
        |
        |import io.mehow.laboratory.Feature
        |import io.mehow.supervisor.Supervisor
        |
        |public enum class FeatureA : Feature<FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  public override val defaultOption: FeatureA
        |    get() = First
        |
        |  public override val supervisorOption: Feature<*> = Supervisor.First
        |}
        |
      """.trimMargin()
    }
  }
})
