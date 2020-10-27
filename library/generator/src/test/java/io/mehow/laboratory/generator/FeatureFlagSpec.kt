package io.mehow.laboratory.generator

import arrow.core.Nel
import arrow.core.identity
import arrow.core.nel
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

internal class FeatureFlagSpec : DescribeSpec({
  val featureBuilder = FeatureFlagModel.Builder(
      visibility = Internal,
      packageName = "io.mehow",
      names = listOf("FeatureA"),
      values = listOf(FeatureValue("First", isDefaultValue = true), FeatureValue("Second")),
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

    context("values") {
      it("cannot be empty") {
        val builder = featureBuilder.copy(values = emptyList())

        val result = builder.build()

        result shouldBeLeft NoFeatureValues(builder.fqcn)
      }

      it("cannot have blank names") {
        val blanks = Arb.stringPattern("([ ]{0,10})")
        checkAll(blanks, blanks, blanks) { valueA, valueB, valueC ->
          val blankNames = Nel(valueA, valueB, valueC)
          val builder = featureBuilder.copy(values = blankNames.toList().map(::FeatureValue))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(blankNames, builder.fqcn)
        }
      }

      it("cannot have names that start with an underscore") {
        checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(values = listOf(name).map(::FeatureValue))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), builder.fqcn)
        }
      }

      it("cannot have names that are not alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
          val builder = featureBuilder.copy(values = listOf(name).map(::FeatureValue))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), builder.fqcn)
        }
      }

      it("can have names that have alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(values = listOf(name).map { FeatureValue(it, isDefaultValue = true) })

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
          val builder = featureBuilder.copy(values = names.toList().map(::FeatureValue))

          val result = builder.build()

          result shouldBeLeft FeatureValuesCollision(Nel(name, nameC), builder.fqcn)
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
      it("cannot have no values") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
        ) { first, second ->
          val builder = featureBuilder.copy(
              values = listOf(FeatureValue(first), FeatureValue(second))
          )
          val result = builder.build()

          result shouldBeLeft NoFeatureDefaultValue(builder.fqcn)
        }
      }

      it("cannot have multiple values") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](2)([a-zA-Z0-9_]{0,10})"),
        ) { first, second, third ->
          val builder = featureBuilder.copy(
              values = listOf(
                  FeatureValue(first, isDefaultValue = true),
                  FeatureValue(second),
                  FeatureValue(third, isDefaultValue = true),
              )
          )
          val result = builder.build()

          result shouldBeLeft MultipleFeatureDefaultValues(Nel(first, third), builder.fqcn)
        }
      }

      it("can have one value") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](2)([a-zA-Z0-9_]{0,10})"),
        ) { first, second, third ->
          val builder = featureBuilder.copy(
              values = listOf(
                  FeatureValue(first),
                  FeatureValue(second),
                  FeatureValue(third, isDefaultValue = true),
              )
          )
          val result = builder.build()

          result.shouldBeRight()
        }
      }
    }
  }

  describe("feature flag source model") {
    context("name") {
      it("cannot be blank") {
        checkAll(Arb.stringPattern("([ ]{0,10})")) { name ->
          val builder = featureBuilder.copy(sourceValues = listOf(FeatureValue(name)))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), "${builder.fqcn}.Source")
        }
      }

      it("cannot start with an underscore") {
        checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(sourceValues = listOf(FeatureValue(name)))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), "${builder.fqcn}.Source")
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
          val builder = featureBuilder.copy(sourceValues = listOf(FeatureValue(name)))

          val result = builder.build()

          result shouldBeLeft InvalidFeatureValues(name.nel(), "${builder.fqcn}.Source")
        }
      }

      it("can contain alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[a-zA-Z][0-9]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(sourceValues = listOf(FeatureValue(name)))

          val result = builder.build()

          result.shouldBeRight()
        }
      }
    }

    context("default") {
      it("can have no values") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
        ) { first, second ->
          val builder = featureBuilder.copy(
              sourceValues = listOf(FeatureValue(first), FeatureValue(second))
          )
          val result = builder.build()

          result.shouldBeRight()
        }
      }

      it("cannot have multiple values") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](2)([a-zA-Z0-9_]{0,10})"),
        ) { first, second, third ->
          val builder = featureBuilder.copy(
              sourceValues = listOf(
                  FeatureValue(first, isDefaultValue = true),
                  FeatureValue(second),
                  FeatureValue(third, isDefaultValue = true),
              )
          )
          val result = builder.build()

          result shouldBeLeft MultipleFeatureDefaultValues(Nel(first, third), "${builder.fqcn}.Source")
        }
      }

      it("can have one value") {
        checkAll(
            Arb.stringPattern("[a-zA-Z](0)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](1)([a-zA-Z0-9_]{0,10})"),
            Arb.stringPattern("[a-zA-Z](2)([a-zA-Z0-9_]{0,10})"),
        ) { first, second, third ->
          val builder = featureBuilder.copy(
              sourceValues = listOf(
                  FeatureValue(first),
                  FeatureValue(second),
                  FeatureValue(third, isDefaultValue = true),
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
      val tempDir = createTempDir()

      val outputFile = featureBuilder.build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import kotlin.Boolean
            |
            |internal enum class FeatureA(
            |  public override val isDefaultValue: Boolean = false
            |) : Feature<FeatureA> {
            |  First(isDefaultValue = true),
            |  Second,
            |  ;
            |}
            |
          """.trimMargin("|")
      }
    }

    it("can be public") {
      val tempDir = createTempDir()
      val builder = featureBuilder.copy(visibility = Public)

      val outputFile = builder.build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import kotlin.Boolean
            |
            |public enum class FeatureA(
            |  public override val isDefaultValue: Boolean = false
            |) : Feature<FeatureA> {
            |  First(isDefaultValue = true),
            |  Second,
            |  ;
            |}
            |
          """.trimMargin("|")
      }
    }

    it("can have sourcedWith parameter") {
      val tempDir = createTempDir()

      val outputFile = featureBuilder
          .copy(sourceValues = listOf(FeatureValue("Remote")))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import java.lang.Class
            |import kotlin.Boolean
            |import kotlin.Suppress
            |
            |internal enum class FeatureA(
            |  public override val isDefaultValue: Boolean = false
            |) : Feature<FeatureA> {
            |  First(isDefaultValue = true),
            |  Second,
            |  ;
            |
            |  @Suppress("UNCHECKED_CAST")
            |  public override val sourcedWith: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
            |
            |  internal enum class Source(
            |    public override val isDefaultValue: Boolean = false
            |  ) : Feature<Source> {
            |    Local(isDefaultValue = true),
            |    Remote,
            |    ;
            |  }
            |}
            |
          """.trimMargin("|")
      }
    }

    it("does not have sourcedWith parameter if only source is Local") {
      val tempDir = createTempDir()

      val outputFile = featureBuilder
          .copy(sourceValues = listOf(FeatureValue("Local")))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import kotlin.Boolean
            |
            |internal enum class FeatureA(
            |  public override val isDefaultValue: Boolean = false
            |) : Feature<FeatureA> {
            |  First(isDefaultValue = true),
            |  Second,
            |  ;
            |}
            |
          """.trimMargin("|")
      }
    }

    it("filters out any custom local source") {
      val tempDir = createTempDir()

      val localPermutations = (0b00000..0b11111).map {
        listOf(it and 0b00001, it and 0b00010, it and 0b00100, it and 0b01000, it and 0b10000)
            .map { mask -> mask != 0 }
            .mapIndexed { index, mask ->
              val char = "local"[index].toString()
              if (mask) char else char.capitalize(Locale.ROOT)
            }.joinToString(separator = "")
      }

      val outputFile = featureBuilder
          .copy(sourceValues = (localPermutations + "Remote").map(::FeatureValue))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import java.lang.Class
            |import kotlin.Boolean
            |import kotlin.Suppress
            |
            |internal enum class FeatureA(
            |  public override val isDefaultValue: Boolean = false
            |) : Feature<FeatureA> {
            |  First(isDefaultValue = true),
            |  Second,
            |  ;
            |
            |  @Suppress("UNCHECKED_CAST")
            |  public override val sourcedWith: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
            |
            |  internal enum class Source(
            |    public override val isDefaultValue: Boolean = false
            |  ) : Feature<Source> {
            |    Local(isDefaultValue = true),
            |    Remote,
            |    ;
            |  }
            |}
            |
          """.trimMargin("|")
      }
    }

    it("allows to set not Local default default for source") {
      val tempDir = createTempDir()

      val outputFile = featureBuilder
          .copy(sourceValues = listOf(FeatureValue("Remote", isDefaultValue = true)))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import java.lang.Class
            |import kotlin.Boolean
            |import kotlin.Suppress
            |
            |internal enum class FeatureA(
            |  public override val isDefaultValue: Boolean = false
            |) : Feature<FeatureA> {
            |  First(isDefaultValue = true),
            |  Second,
            |  ;
            |
            |  @Suppress("UNCHECKED_CAST")
            |  public override val sourcedWith: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
            |
            |  internal enum class Source(
            |    public override val isDefaultValue: Boolean = false
            |  ) : Feature<Source> {
            |    Local,
            |    Remote(isDefaultValue = true),
            |    ;
            |  }
            |}
            |
          """.trimMargin("|")
      }
    }

    it("source visibility follows feature visibility") {
      val tempDir = createTempDir()

      val outputFile = featureBuilder
          .copy(visibility = Public, sourceValues = listOf(FeatureValue("Remote")))
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import java.lang.Class
            |import kotlin.Boolean
            |import kotlin.Suppress
            |
            |public enum class FeatureA(
            |  public override val isDefaultValue: Boolean = false
            |) : Feature<FeatureA> {
            |  First(isDefaultValue = true),
            |  Second,
            |  ;
            |
            |  @Suppress("UNCHECKED_CAST")
            |  public override val sourcedWith: Class<Feature<*>> = Source::class.java as Class<Feature<*>>
            |
            |  public enum class Source(
            |    public override val isDefaultValue: Boolean = false
            |  ) : Feature<Source> {
            |    Local(isDefaultValue = true),
            |    Remote,
            |    ;
            |  }
            |}
            |
          """.trimMargin("|")
      }
    }

    it("can have description") {
      val tempDir = createTempDir()

      val outputFile = featureBuilder
          .copy(description = "Feature description")
          .build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |import kotlin.Boolean
            |import kotlin.String
            |
            |internal enum class FeatureA(
            |  public override val isDefaultValue: Boolean = false
            |) : Feature<FeatureA> {
            |  First(isDefaultValue = true),
            |  Second,
            |  ;
            |
            |  public override val description: String = "Feature description"
            |}
            |
          """.trimMargin("|")
      }
    }
  }
})
