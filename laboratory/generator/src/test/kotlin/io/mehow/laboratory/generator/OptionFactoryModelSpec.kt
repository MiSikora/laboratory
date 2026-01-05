package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.generator.test.shouldSpecify

class OptionFactoryModelSpec :
  FunSpec({
    test("internal visibility") {
      val model =
        OptionFactoryModel(
          ClassName("io.mehow", "Factory"),
          listOf(
            FeatureFlagModel(
              ClassName("io.mehow", "FeatureA"),
              listOf(FeatureFlagOption("A", isDefault = true)),
            )
          ),
          Internal,
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = when (key) {
            "io.mehow.FeatureA" -> when (name) {
              "A" -> FeatureA.A
              else -> null
            }
            else -> null
          }
        }
        """
    }

    test("public visibility") {
      val model =
        OptionFactoryModel(
          ClassName("io.mehow", "Factory"),
          listOf(
            FeatureFlagModel(
              ClassName("io.mehow", "FeatureA"),
              listOf(FeatureFlagOption("A", isDefault = true)),
            )
          ),
          Public,
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.String

        public fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = when (key) {
            "io.mehow.FeatureA" -> when (name) {
              "A" -> FeatureA.A
              else -> null
            }
            else -> null
          }
        }
        """
    }

    test("single feature") {
      val model =
        OptionFactoryModel(
          ClassName("io.mehow", "Factory"),
          listOf(
            FeatureFlagModel(
              ClassName("io.mehow", "FeatureA"),
              listOf(FeatureFlagOption("A", isDefault = true), FeatureFlagOption("B")),
            )
          ),
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = when (key) {
            "io.mehow.FeatureA" -> when (name) {
              "A" -> FeatureA.A
              "B" -> FeatureA.B
              else -> null
            }
            else -> null
          }
        }
        """
    }

    test("multiple features") {
      val model =
        OptionFactoryModel(
          ClassName("io.mehow", "Factory"),
          listOf(
            FeatureFlagModel(
              ClassName("io.mehow", "FeatureA"),
              listOf(FeatureFlagOption("OneA", isDefault = true), FeatureFlagOption("OneB")),
            ),
            FeatureFlagModel(
              ClassName("io.mehow", "FeatureB"),
              listOf(FeatureFlagOption("TwoA", isDefault = true), FeatureFlagOption("TwoB")),
            ),
          ),
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = when (key) {
            "io.mehow.FeatureA" -> when (name) {
              "OneA" -> FeatureA.OneA
              "OneB" -> FeatureA.OneB
              else -> null
            }
            "io.mehow.FeatureB" -> when (name) {
              "TwoA" -> FeatureB.TwoA
              "TwoB" -> FeatureB.TwoB
              else -> null
            }
            else -> null
          }
        }
        """
    }

    test("no features") {
      val model = OptionFactoryModel(ClassName("io.mehow", "Factory"), features = emptyList())

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = null
        }
        """
    }

    test("feature with different package") {
      val model =
        OptionFactoryModel(
          ClassName("io.mehow", "Factory"),
          listOf(
            FeatureFlagModel(
              ClassName("io.mehow.other", "FeatureA"),
              listOf(FeatureFlagOption("A", isDefault = true)),
            )
          ),
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import io.mehow.other.FeatureA
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = when (key) {
            "io.mehow.other.FeatureA" -> when (name) {
              "A" -> FeatureA.A
              else -> null
            }
            else -> null
          }
        }
        """
    }

    test("custom keys") {
      val model =
        OptionFactoryModel(
          ClassName("io.mehow", "Factory"),
          listOf(
            FeatureFlagModel(
              ClassName("io.mehow", "FeatureA"),
              listOf(FeatureFlagOption("OneA", isDefault = true)),
              key = "custom-key-1",
            ),
            FeatureFlagModel(
              ClassName("io.mehow", "FeatureB"),
              listOf(FeatureFlagOption("TwoA", isDefault = true)),
              key = "custom-key-2",
            ),
          ),
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = when (key) {
            "custom-key-1" -> when (name) {
              "OneA" -> FeatureA.OneA
              else -> null
            }
            "custom-key-2" -> when (name) {
              "TwoA" -> FeatureB.TwoA
              else -> null
            }
            else -> null
          }
        }
        """
    }

    test("duplicate keys") {
      val exception =
        shouldThrow<IllegalArgumentException> {
          OptionFactoryModel(
            ClassName("io.mehow", "Factory"),
            listOf(
              FeatureFlagModel(
                ClassName("io.mehow", "FeatureA"),
                listOf(FeatureFlagOption("A", isDefault = true)),
                key = "custom-key-1",
              ),
              FeatureFlagModel(
                ClassName("io.mehow", "FeatureB"),
                listOf(FeatureFlagOption("A", isDefault = true)),
                key = "custom-key-1",
              ),
              FeatureFlagModel(
                ClassName("io.mehow", "FeatureC"),
                listOf(FeatureFlagOption("A", isDefault = true)),
                key = "custom-key-1",
              ),
              FeatureFlagModel(
                ClassName("io.mehow", "FeatureD"),
                listOf(FeatureFlagOption("A", isDefault = true)),
                key = "custom-key-2",
              ),
              FeatureFlagModel(
                ClassName("io.mehow", "FeatureE"),
                listOf(FeatureFlagOption("A", isDefault = true)),
                key = "custom-key-2",
              ),
              FeatureFlagModel(
                ClassName("io.mehow", "FeatureF"),
                listOf(FeatureFlagOption("A", isDefault = true)),
                key = "custom-key-3",
              ),
            ),
          )
        }

      exception shouldHaveMessage
        """
        Feature flags must have unique keys. Found following duplicates:
         - custom-key-1: [io.mehow.FeatureA, io.mehow.FeatureB, io.mehow.FeatureC]
         - custom-key-2: [io.mehow.FeatureD, io.mehow.FeatureE]
        """
          .trimIndent()
    }

    test("key matching self fqcn") {
      val model =
        OptionFactoryModel(
          ClassName("io.mehow", "Factory"),
          listOf(
            FeatureFlagModel(
              ClassName("io.mehow", "FeatureA"),
              listOf(FeatureFlagOption("A", isDefault = true)),
              key = "io.mehow.FeatureA",
            )
          ),
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = when (key) {
            "io.mehow.FeatureA" -> when (name) {
              "A" -> FeatureA.A
              else -> null
            }
            else -> null
          }
        }
        """
    }

    test("key matching other fqcn") {
      val exception =
        shouldThrow<IllegalArgumentException> {
          OptionFactoryModel(
            ClassName("io.mehow", "Factory"),
            listOf(
              FeatureFlagModel(
                ClassName("io.mehow", "FeatureA"),
                listOf(FeatureFlagOption("A", isDefault = true)),
              ),
              FeatureFlagModel(
                ClassName("io.mehow", "FeatureB"),
                listOf(FeatureFlagOption("A", isDefault = true)),
                key = "io.mehow.FeatureA",
              ),
            ),
          )
        }

      exception shouldHaveMessage
        """
        Feature flags must have unique keys. Found following duplicates:
         - io.mehow.FeatureA: [io.mehow.FeatureA, io.mehow.FeatureB]
        """
          .trimIndent()
    }
  })
