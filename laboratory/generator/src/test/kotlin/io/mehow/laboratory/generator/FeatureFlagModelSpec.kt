package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
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

class FeatureFlagModelSpec : FunSpec({
  test("can be internal") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |}
      |
    """.trimMargin()
  }

  test("can be public") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |}
      |
    """.trimMargin()
  }

  test("can have single option") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |}
      |
    """.trimMargin()
  }

  test("can have source") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |
      |  override val source: Class<out Feature<*>> = Source::class.java
      |
      |  public enum class Source : Feature<Source> {
      |    Local,
      |    Remote,
      |    ;
      |
      |    override val defaultOption: Source
      |      get() = Local
      |  }
      |}
      |
    """.trimMargin()
  }

  test("does not have source parameter if only source is Local") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |}
      |
    """.trimMargin()
  }

  test("filters out any custom local source") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |
      |  override val source: Class<out Feature<*>> = Source::class.java
      |
      |  public enum class Source : Feature<Source> {
      |    Local,
      |    Remote,
      |    ;
      |
      |    override val defaultOption: Source
      |      get() = Local
      |  }
      |}
      |
    """.trimMargin()
  }

  test("can change default source") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |
      |  override val source: Class<out Feature<*>> = Source::class.java
      |
      |  public enum class Source : Feature<Source> {
      |    Local,
      |    Remote,
      |    ;
      |
      |    override val defaultOption: Source
      |      get() = Remote
      |  }
      |}
      |
    """.trimMargin()
  }

  test("copies feature visibility to source visibility") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |
      |  override val source: Class<out Feature<*>> = Source::class.java
      |
      |  internal enum class Source : Feature<Source> {
      |    Local,
      |    Remote,
      |    ;
      |
      |    override val defaultOption: Source
      |      get() = Local
      |  }
      |}
      |
    """.trimMargin()
  }

  test("can have supervisor") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |
      |  override val supervisorOption: Feature<*> = Supervisor.First
      |}
      |
    """.trimMargin()
  }

  test("description is added as KDoc") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |
      |  override val description: String = "Feature description"
      |}
      |
    """.trimMargin()
  }

  test("description does not break hyperlinks") {
    @Suppress("MaxLineLength")
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |
      |  override val description: String =
      |      "Some [long hyperlink](https://square.github.io/kotlinpoet/1.x/kotlinpoet-classinspector-elements/com.squareup.kotlinpoet.classinspector.elements/) in the KDoc."
      |}
      |
    """.trimMargin()
  }

  test("uses warning level as a default deprecation") {
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
      |  override val defaultOption: FeatureA
      |    get() = First
      |}
      |
    """.trimMargin()
  }

  enumValues<DeprecationLevel>().forEach { level ->
    test("can use explicit $level deprecation level") {
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
        |  level = DeprecationLevel.$level,
        |)
        |public enum class FeatureA : Feature<@Suppress("$suppressLevel") FeatureA> {
        |  First,
        |  Second,
        |  ;
        |
        |  @Suppress("$suppressLevel")
        |  override val defaultOption: FeatureA
        |    get() = First
        |}
        |
      """.trimMargin()
    }
  }

  test("fails to generate when there are no options") {
    val exception = shouldThrow<IllegalArgumentException> {
      FeatureFlagModel(
        ClassName("io.mehow", "FeatureA"),
        options = emptyList(),
      )
    }

    exception shouldHaveMessage "io.mehow.FeatureA must have at least one option"
  }

  test("fails to generate when there is no default option") {
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

  test("fails to generate when there are multiple default options") {
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

  test("fails to supervise itself") {
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
})
