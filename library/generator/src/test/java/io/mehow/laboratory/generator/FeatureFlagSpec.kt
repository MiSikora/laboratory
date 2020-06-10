package io.mehow.laboratory.generator

import arrow.core.Nel
import arrow.core.getOrElse
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

class FeatureFlagSpec : DescribeSpec({
  val flagBuilder = FeatureFlagModel.Builder(
    visibility = Internal,
    packageName = "io.mehow",
    name = "Feature",
    values = listOf("First", "Second")
  )

  describe("feature flag model") {
    context("name") {
      it("cannot be blank") {
        checkAll(Arb.stringPattern("([ ]{0,10})")) { name ->
          val builder = flagBuilder.copy(name = name)

          val result = builder.build()

          result shouldBeLeft InvalidFlagName(name, builder.fqcn)
        }
      }

      it("cannot start with an underscore") {
        checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = flagBuilder.copy(name = name)

          val result = builder.build()

          result shouldBeLeft InvalidFlagName(name, builder.fqcn)
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
          val builder = flagBuilder.copy(name = name)

          val result = builder.build()

          result shouldBeLeft InvalidFlagName(name, builder.fqcn)
        }
      }

      it("can contain alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = flagBuilder.copy(name = name)

          val result = builder.build()

          result.shouldBeRight()
        }
      }
    }

    context("package name") {
      it("can be empty") {
        val builder = flagBuilder.copy(packageName = "")

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
          val builder = flagBuilder.copy(packageName = packageName)

          val result = builder.build()

          result.shouldBeRight()
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { packageName ->
          val builder = flagBuilder.copy(packageName = packageName)

          val result = builder.build()

          result shouldBeLeft InvalidPackageName(builder.fqcn)
        }
      }

      context("values") {
        it("cannot be empty") {
          val builder = flagBuilder.copy(values = emptyList())

          val result = builder.build()

          result shouldBeLeft NoFlagValues(builder.fqcn)
        }

        it("cannot have blank names") {
          val blanks = Arb.stringPattern("([ ]{0,10})")
          checkAll(blanks, blanks, blanks) { valueA, valueB, valueC ->
            val blankNames = Nel(valueA, valueB, valueC)
            val builder = flagBuilder.copy(values = blankNames.toList())

            val result = builder.build()

            result shouldBeLeft InvalidFlagValues(blankNames, builder.fqcn)
          }
        }

        it("cannot have names that start with an underscore") {
          checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
            val builder = flagBuilder.copy(values = listOf(name))

            val result = builder.build()

            result shouldBeLeft InvalidFlagValues(name.nel(), builder.fqcn)
          }
        }

        it("cannot have names that are not alphanumeric characters or underscores") {
          checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
            val builder = flagBuilder.copy(values = listOf(name))

            val result = builder.build()

            result shouldBeLeft InvalidFlagValues(name.nel(), builder.fqcn)
          }
        }

        it("can have names that have alphanumeric characters or underscores") {
          checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
            val builder = flagBuilder.copy(values = listOf(name))

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
            val builder = flagBuilder.copy(values = names.toList())

            val result = builder.build()

            result shouldBeLeft FlagNameCollision(Nel(name, nameC), builder.fqcn)
          }
        }
      }

      context("group") {
        it("can have unique features") {
          checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
            val nameA = name + "A"
            val builder = flagBuilder.copy(name = name)
            val builderA = flagBuilder.copy(name = nameA)
            val builderB = flagBuilder.copy(packageName = name)
            val builderC = flagBuilder.copy(packageName = nameA)
            val builders = listOf(builder, builderA, builderB, builderC)

            val result = builders.buildAll()

            result.shouldBeRight()
          }
        }

        it("cannot have namespace collisions") {
          checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
            val nameA = name + "A"
            val nameB = name + "B"
            val builder = flagBuilder.copy(name = name)
            val builderA = flagBuilder.copy(name = nameA)
            val builderB = flagBuilder.copy(name = nameB)
            val builderC = flagBuilder.copy(packageName = name)
            val builders = listOf(builder, builder, builderA, builderB, builderC)
            val duplicate = builder.build().getOrElse { error("Should be right") }

            val result = builders.buildAll()

            result shouldBeLeft FlagNamespaceCollision(duplicate.nel())
          }
        }
      }
    }
  }

  describe("generated feature flag") {
    it("can be internal") {
      val tempDir = createTempDir()

      val outputFile = flagBuilder.build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |
            |@Feature
            |internal enum class Feature {
            |  First,
            |
            |  Second
            |}
            |
          """.trimMargin("|")
      }
    }

    it("can be public") {
      val tempDir = createTempDir()
      val builder = flagBuilder.copy(visibility = Public)

      val outputFile = builder.build().map { model -> model.generate(tempDir) }

      outputFile shouldBeRight { file ->
        file.readText() shouldBe """
            |package io.mehow
            |
            |import io.mehow.laboratory.Feature
            |
            |@Feature
            |enum class Feature {
            |  First,
            |
            |  Second
            |}
            |
          """.trimMargin("|")
      }
    }
  }
})
