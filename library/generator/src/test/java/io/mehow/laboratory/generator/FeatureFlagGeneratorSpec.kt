package io.mehow.laboratory.generator

import arrow.core.getOrElse
import arrow.core.identity
import arrow.core.nel
import arrow.core.nonEmptyListOf
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import java.util.Locale
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.HIDDEN
import kotlin.DeprecationLevel.WARNING
import kotlin.io.path.createTempDirectory

internal class FeatureFlagGeneratorSpec : DescribeSpec({
  val featureBuilder = FeatureFlagModel.Builder(
      visibility = Internal,
      packageName = "io.mehow",
      names = listOf("FeatureA"),
      options = listOf(FeatureFlagOption("First", isDefault = true), FeatureFlagOption("Second")),
  )

  describe("feature flag model") {
    context("name") {
      it("cannot be blank") {
        checkAll(Arb.stringPattern("([ ]{0,10})")) { name ->
          val builder = featureBuilder.copy(names = listOf(name))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureName(name, builder.fqcn)
        }
      }

      it("cannot start with an underscore") {
        checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(names = listOf(name))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureName(name, builder.fqcn)
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
          val builder = featureBuilder.copy(names = listOf(name))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureName(name, builder.fqcn)
        }
      }

      it("can contain alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(names = listOf(name))

          val result = builder.build()

          result.shouldBeRight()
        }
      }
    }

    context("package name") {
      it("can be empty") {
        val builder = featureBuilder.copy(packageName = "")

        val result = builder.build()

        result.shouldBeRight()
      }

      it("can be valid java package name") {
        val packagePart = Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")
        val packageCount = Arb.int(1..10)
        checkAll(packagePart, packageCount) { part, count ->
          val packageName = List(count, ::identity).joinToString(".") {
            part.take(1) + part.drop(1).toList().shuffled().joinToString("")
          }
          val builder = featureBuilder.copy(packageName = packageName)

          val result = builder.build()

          result.shouldBeRight()
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { packageName ->
          val builder = featureBuilder.copy(packageName = packageName)

          val result = builder.build()

          result shouldBeLeft InvalidPackageName(builder.fqcn)
        }
      }
    }

    context("options") {
      it("cannot be empty") {
        val builder = featureBuilder.copy(options = emptyList())

        val result = builder.build()

        result shouldBeLeft NoFeatureValues(builder.fqcn)
      }

      it("cannot have blank names") {
        val blanks = Arb.stringPattern("([ ]{0,10})")
        checkAll(blanks, blanks, blanks) { optionA, optionB, optionC ->
          val blankNames = nonEmptyListOf(optionA, optionB, optionC)
          val builder = featureBuilder.copy(options = blankNames.toList().map(::FeatureFlagOption))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(blankNames, builder.fqcn)
        }
      }

      it("cannot have names that start with an underscore") {
        checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(options = listOf(name).map(::FeatureFlagOption))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), builder.fqcn)
        }
      }

      it("cannot have names that are not alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
          val builder = featureBuilder.copy(options = listOf(name).map(::FeatureFlagOption))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), builder.fqcn)
        }
      }

      it("can have names that have alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(options = listOf(name).map { FeatureFlagOption(it, isDefault = true) })

          val result = builder.build()

          result.shouldBeRight()
        }
      }

      it("cannot have duplicates") {
        checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
          val nameA = name + "A"
          val nameB = name + "B"
          val nameC = name + "C"
          val names = listOf(name, name, nameA, nameB, nameC, nameC, nameC)
          val builder = featureBuilder.copy(options = names.toList().map(::FeatureFlagOption))

          val result = builder.build()

          result shouldBeLeft FeatureValuesCollision(nonEmptyListOf(name, nameC), builder.fqcn)
        }
      }
    }

    context("group") {
      it("can have unique features") {
        checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
          val nameA = name + "A"
          val builder = featureBuilder.copy(names = listOf(name))
          val builderA = featureBuilder.copy(names = listOf(nameA))
          val builderB = featureBuilder.copy(packageName = name)
          val builderC = featureBuilder.copy(packageName = nameA)
          val builders = listOf(builder, builderA, builderB, builderC)

          val result = builders.buildAll()

          result.shouldBeRight()
        }
      }

      it("cannot have namespace collisions") {
        checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
          val nameA = name + "A"
          val nameB = name + "B"
          val builder = featureBuilder.copy(names = listOf(name))
          val builderA = featureBuilder.copy(names = listOf(nameA))
          val builderB = featureBuilder.copy(names = listOf(nameB))
          val builderC = featureBuilder.copy(packageName = name)
          val builders = listOf(builder, builder, builderA, builderB, builderC)

          val result = builders.buildAll()

          result shouldBeLeft FeaturesCollision(builder.fqcn.nel())
        }
      }
    }

    context("default") {
      it("cannot have no options") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
        ) { first, second ->
          val builder = featureBuilder.copy(
              options = listOf(FeatureFlagOption(first), FeatureFlagOption(second))
          )
          val result = builder.build()

          result shouldBeLeft NoFeatureDefaultValue(builder.fqcn)
        }
      }

      it("cannot have multiple options") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](2)([a-zA-Z0-9_]{0,10})"),
        ) { first, second, third ->
          val builder = featureBuilder.copy(
              options = listOf(
                  FeatureFlagOption(first, isDefault = true),
                  FeatureFlagOption(second),
                  FeatureFlagOption(third, isDefault = true),
              )
          )
          val result = builder.build()

          result shouldBeLeft MultipleFeatureDefaultValues(nonEmptyListOf(first, third), builder.fqcn)
        }
      }

      it("can have one option") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](2)([a-zA-Z0-9_]{0,10})"),
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
      it("must be present in parent") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
        ) { first, second ->
          val options = listOf(FeatureFlagOption(first, isDefault = true), FeatureFlagOption(second))
          val parent = featureBuilder.copy(names = listOf("Parent"), options = options)
              .build().getOrElse { error("Should be right") }
          val builder = featureBuilder.copy(supervisor = Supervisor.Builder(parent, options[0]))

          val result = builder.build()

          result.shouldBeRight()
        }
      }

      it("cannot be absent in parent") {
        checkAll(Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})")) { optionName ->
          val parent = featureBuilder.copy(names = listOf("Parent"))
              .build().getOrElse { error("Should be right") }
          val option = FeatureFlagOption(optionName)
          val builder = featureBuilder.copy(supervisor = Supervisor.Builder(parent, option))

          val result = builder.build()

          result shouldBeLeft NoMatchingOptionFound(parent.toString(), optionName)
        }
      }

      it("cannot self-supervise") {
        val parent = featureBuilder.build().getOrElse { error("Should be right") }
        val supervisor = Supervisor.Builder(parent, parent.options.head)
        val builder = featureBuilder.copy(supervisor = supervisor)

        val result = builder.build()

        result shouldBeLeft SelfSupervision(parent.toString())
      }
    }
  }

  describe("feature flag source model") {
    context("name") {
      it("cannot be blank") {
        checkAll(Arb.stringPattern("([ ]{0,10})")) { name ->
          val builder = featureBuilder.copy(sourceOptions = listOf(FeatureFlagOption(name)))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), "${builder.fqcn}.Source")
        }
      }

      it("cannot start with an underscore") {
        checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(sourceOptions = listOf(FeatureFlagOption(name)))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), "${builder.fqcn}.Source")
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
          val builder = featureBuilder.copy(sourceOptions = listOf(FeatureFlagOption(name)))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), "${builder.fqcn}.Source")
        }
      }

      it("can contain alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[a-zA-Z][0-9]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(sourceOptions = listOf(FeatureFlagOption(name)))

          val result = builder.build()

          result.shouldBeRight()
        }
      }
    }

    context("default") {
      it("can have no options") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
        ) { first, second ->
          val builder = featureBuilder.copy(
              sourceOptions = listOf(FeatureFlagOption(first), FeatureFlagOption(second))
          )
          val result = builder.build()

          result.shouldBeRight()
        }
      }

      it("cannot have multiple options") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](2)([a-zA-Z0-9_]{0,10})"),
        ) { first, second, third ->
          val builder = featureBuilder.copy(
              sourceOptions = listOf(
                  FeatureFlagOption(first, isDefault = true),
                  FeatureFlagOption(second),
                  FeatureFlagOption(third, isDefault = true),
              )
          )
          val result = builder.build()

          result shouldBeLeft MultipleFeatureDefaultValues(nonEmptyListOf(first, third), "${builder.fqcn}.Source")
        }
      }

      it("can have one option") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](2)([a-zA-Z0-9_]{0,10})"),
        ) { first, second, third ->
          val builder = featureBuilder.copy(
              sourceOptions = listOf(
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

  }

  describe("generated feature flag") {
    it("can be internal") {
      val tempDir = createTempDirectory().toFile()

      val outputFile = featureBuilder.build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
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
          """.trimMargin("|")
      }
    }

    it("can be public") {
      val tempDir = createTempDirectory().toFile()
      val builder = featureBuilder.copy(visibility = Public)

      val outputFile = builder.build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
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
          """.trimMargin("|")
      }
    }

    it("can have source parameter") {
      val tempDir = createTempDirectory().toFile()

      val outputFile = featureBuilder
          .copy(sourceOptions = listOf(FeatureFlagOption("Remote")))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import java.lang.Class
            |import kotlin.Suppress
            |
            |internal enum class FeatureA : Feature<FeatureA> {
            |  First,
            |  Second,
            |  ;
            |
            |  public override val defaultOption: FeatureA
            |    get() = First
            |
            |  @Suppress("UNCHECKED_CAST")
            |  public override val source: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
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
          """.trimMargin("|")
      }
    }

    it("does not have source parameter if only source is Local") {
      val tempDir = createTempDirectory().toFile()

      val outputFile = featureBuilder
          .copy(sourceOptions = listOf(FeatureFlagOption("Local")))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
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
          """.trimMargin("|")
      }
    }

    it("filters out any custom local source") {
      val tempDir = createTempDirectory().toFile()

      val localPermutations = (0b00000..0b11111).map {
        listOf(it and 0b00001, it and 0b00010, it and 0b00100, it and 0b01000, it and 0b10000)
            .map { mask -> mask != 0 }
            .mapIndexed { index, mask ->
              val char = "local"[index].toString()
              if (mask) char else char.capitalize(Locale.ROOT)
            }.joinToString(separator = "")
      }

      val outputFile = featureBuilder
          .copy(sourceOptions = (localPermutations + "Remote").map(::FeatureFlagOption))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import java.lang.Class
            |import kotlin.Suppress
            |
            |internal enum class FeatureA : Feature<FeatureA> {
            |  First,
            |  Second,
            |  ;
            |
            |  public override val defaultOption: FeatureA
            |    get() = First
            |
            |  @Suppress("UNCHECKED_CAST")
            |  public override val source: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
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
          """.trimMargin("|")
      }
    }

    it("allows to set not Local default default for source") {
      val tempDir = createTempDirectory().toFile()

      val outputFile = featureBuilder
          .copy(sourceOptions = listOf(FeatureFlagOption("Remote", isDefault = true)))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import java.lang.Class
            |import kotlin.Suppress
            |
            |internal enum class FeatureA : Feature<FeatureA> {
            |  First,
            |  Second,
            |  ;
            |
            |  public override val defaultOption: FeatureA
            |    get() = First
            |
            |  @Suppress("UNCHECKED_CAST")
            |  public override val source: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
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
          """.trimMargin("|")
      }
    }

    it("source visibility follows feature visibility") {
      val tempDir = createTempDirectory().toFile()

      val outputFile = featureBuilder
          .copy(visibility = Public, sourceOptions = listOf(FeatureFlagOption("Remote")))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import java.lang.Class
            |import kotlin.Suppress
            |
            |public enum class FeatureA : Feature<FeatureA> {
            |  First,
            |  Second,
            |  ;
            |
            |  public override val defaultOption: FeatureA
            |    get() = First
            |
            |  @Suppress("UNCHECKED_CAST")
            |  public override val source: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
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
          """.trimMargin("|")
      }
    }

    context("description") {
      it("is added as KDoc") {
        val tempDir = createTempDirectory().toFile()

        val outputFile = featureBuilder
            .copy(description = "Feature description")
            .build().map { model -> model.generate(tempDir) }

        outputFile shouldBeRight { file ->
          file.readText() shouldBe """
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
          """.trimMargin("|")
        }
      }

      it("does not break hyperlinks") {
        val tempDir = createTempDirectory().toFile()

        val outputFile = featureBuilder
            .copy(
                description = "Some [long hyperlink](https://square.github.io/kotlinpoet/1.x/kotlinpoet-classinspector-elements/com.squareup.kotlinpoet.classinspector.elements/) in the KDoc.")
            .build().map { model -> model.generate(tempDir) }

        outputFile shouldBeRight { file ->
          file.readText() shouldBe """
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
          """.trimMargin("|")
        }
      }
    }

    context("can be deprecated") {
      it("with warning level by default") {
        val tempDir = createTempDirectory().toFile()

        val outputFile = featureBuilder
            .copy(deprecation = Deprecation(message = "Deprecation message"))
            .build().map { model -> model.generate(tempDir) }

        outputFile shouldBeRight { file ->
          file.readText() shouldBe """
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
          """.trimMargin("|")
        }
      }

      enumValues<DeprecationLevel>().forEach { level ->
        it("with explicit $level deprecation level") {
          val tempDir = createTempDirectory().toFile()

          val outputFile = featureBuilder
              .copy(deprecation = Deprecation(message = "Deprecation message", level = level))
              .build().map { model -> model.generate(tempDir) }

          val suppressLevel = when (level) {
            WARNING -> "DEPRECATION"
            ERROR, HIDDEN -> "DEPRECATION_ERROR"
          }

          outputFile shouldBeRight { file ->
            file.readText() shouldBe """
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
          """.trimMargin("|")
          }
        }
      }
    }

    it("can have supervisor") {
      val tempDir = createTempDirectory().toFile()

      val parent = featureBuilder.copy(packageName = "io.mehow.parent", names = listOf("Parent"))
          .build().getOrElse { error("Should be right") }
      val option = FeatureFlagOption("First")

      val outputFile = featureBuilder
          .copy(supervisor = Supervisor.Builder(parent, option))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
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
          """.trimMargin("|")
      }
    }
  }
})
