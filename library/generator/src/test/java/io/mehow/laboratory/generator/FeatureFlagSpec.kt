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

class FeatureFlagSpec : DescribeSpec({
  val featureBuilder = FeatureFlagModel.Builder(
    visibility = Internal,
    packageName = "io.mehow",
    name = "Feature",
    values = listOf("First", "Second")
  )

  describe("feature flag model") {
    context("name") {
      it("cannot be blank") {
        checkAll(Arb.stringPattern("([ ]{0,10})")) { name ->
          val builder = featureBuilder.copy(name = name)

          val result = builder.build()

          result shouldBeLeft InvalidFeatureName(name, builder.fqcn)
        }
      }

      it("cannot start with an underscore") {
        checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(name = name)

          val result = builder.build()

          result shouldBeLeft InvalidFeatureName(name, builder.fqcn)
        }
      }

      it("cannot contain characters that are not alphanumeric or underscores") {
        checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
          val builder = featureBuilder.copy(name = name)

          val result = builder.build()

          result shouldBeLeft InvalidFeatureName(name, builder.fqcn)
        }
      }

      it("can contain alphanumeric characters or underscores") {
        checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
          val builder = featureBuilder.copy(name = name)

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
            val builder = featureBuilder.copy(values = blankNames.toList())

            val result = builder.build()

            result shouldBeLeft InvalidFeatureValues(blankNames, builder.fqcn)
          }
        }

        it("cannot have names that start with an underscore") {
          checkAll(Arb.stringPattern("[_]([a-zA-Z0-9_]{0,10})")) { name ->
            val builder = featureBuilder.copy(values = listOf(name))

            val result = builder.build()

            result shouldBeLeft InvalidFeatureValues(name.nel(), builder.fqcn)
          }
        }

        it("cannot have names that are not alphanumeric characters or underscores") {
          checkAll(Arb.stringPattern("[^a-zA-Z0-9_]")) { name ->
            val builder = featureBuilder.copy(values = listOf(name))

            val result = builder.build()

            result shouldBeLeft InvalidFeatureValues(name.nel(), builder.fqcn)
          }
        }

        it("can have names that have alphanumeric characters or underscores") {
          checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
            val builder = featureBuilder.copy(values = listOf(name))

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
            val builder = featureBuilder.copy(values = names.toList())

            val result = builder.build()

            result shouldBeLeft FeatureValuesCollision(Nel(name, nameC), builder.fqcn)
          }
        }
      }

      context("group") {
        it("can have unique features") {
          checkAll(Arb.stringPattern("[a-zA-Z]([a-zA-Z0-9_]{0,10})")) { name ->
            val nameA = name + "A"
            val builder = featureBuilder.copy(name = name)
            val builderA = featureBuilder.copy(name = nameA)
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
            val builder = featureBuilder.copy(name = name)
            val builderA = featureBuilder.copy(name = nameA)
            val builderB = featureBuilder.copy(name = nameB)
            val builderC = featureBuilder.copy(packageName = name)
            val builders = listOf(builder, builder, builderA, builderB, builderC)

            val result = builders.buildAll()

            result shouldBeLeft FeaturesCollision(builder.fqcn.nel())
          }
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
      val builder = featureBuilder.copy(visibility = Public)

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
