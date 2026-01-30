package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.generator.test.shouldSpecify
import org.junit.Test

class OptionFactoryModelTest {
  @Test
  fun `internal visibility`() {
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
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = null
        }
        """
  }

  @Test
  fun `public visibility`() {
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
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = null
        }
        """
  }

  @Test
  fun `single feature`() {
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
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = null
        }
        """
  }

  @Test
  fun `single binary feature`() {
    val model =
      OptionFactoryModel(
        ClassName("io.mehow", "Factory"),
        listOf(
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("A", isDefault = true), FeatureFlagOption("B")),
            trueOption = FeatureFlagTrueOption("A"),
          )
        ),
      )

    val fileSpec = model.prepare()

    fileSpec shouldSpecify
      """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = when (key) {
            "io.mehow.FeatureA" -> if (binaryValue) FeatureA.A else FeatureA.B
            else -> null
          }
        }
        """
  }

  @Test
  fun `multiple features`() {
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
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = null
        }
        """
  }

  @Test
  fun `multiple binary features`() {
    val model =
      OptionFactoryModel(
        ClassName("io.mehow", "Factory"),
        listOf(
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("OneA", isDefault = true), FeatureFlagOption("OneB")),
            trueOption = FeatureFlagTrueOption("OneA"),
          ),
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureB"),
            listOf(FeatureFlagOption("TwoA", isDefault = true), FeatureFlagOption("TwoB")),
            trueOption = FeatureFlagTrueOption("TwoA"),
          ),
        ),
      )

    val fileSpec = model.prepare()

    fileSpec shouldSpecify
      """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = when (key) {
            "io.mehow.FeatureA" -> if (binaryValue) FeatureA.OneA else FeatureA.OneB
            "io.mehow.FeatureB" -> if (binaryValue) FeatureB.TwoA else FeatureB.TwoB
            else -> null
          }
        }
        """
  }

  @Test
  fun `mixed features`() {
    val model =
      OptionFactoryModel(
        ClassName("io.mehow", "Factory"),
        listOf(
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("OneA", isDefault = true), FeatureFlagOption("OneB")),
            trueOption = FeatureFlagTrueOption("OneA"),
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
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = when (key) {
            "io.mehow.FeatureA" -> if (binaryValue) FeatureA.OneA else FeatureA.OneB
            else -> null
          }
        }
        """
  }

  @Test
  fun `no features`() {
    val model = OptionFactoryModel(ClassName("io.mehow", "Factory"), features = emptyList())

    val fileSpec = model.prepare()

    fileSpec shouldSpecify
      """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.OptionFactory
        import kotlin.Boolean
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = null

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = null
        }
        """
  }

  @Test
  fun `feature with different package`() {
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
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = null
        }
        """
  }

  @Test
  fun `binary feature with different package`() {
    val model =
      OptionFactoryModel(
        ClassName("io.mehow", "Factory"),
        listOf(
          FeatureFlagModel(
            ClassName("io.mehow.other", "FeatureA"),
            listOf(FeatureFlagOption("A", isDefault = true), FeatureFlagOption("B")),
            trueOption = FeatureFlagTrueOption("A"),
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
        import kotlin.Boolean
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = when (key) {
            "io.mehow.other.FeatureA" -> when (name) {
              "A" -> FeatureA.A
              "B" -> FeatureA.B
              else -> null
            }
            else -> null
          }

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = when (key) {
            "io.mehow.other.FeatureA" -> if (binaryValue) FeatureA.A else FeatureA.B
            else -> null
          }
        }
        """
  }

  @Test
  fun `feature with custom key`() {
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
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = null
        }
        """
  }

  @Test
  fun `binary feature with custom key`() {
    val model =
      OptionFactoryModel(
        ClassName("io.mehow", "Factory"),
        listOf(
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("OneA", isDefault = true), FeatureFlagOption("OneB")),
            trueOption = FeatureFlagTrueOption("OneA"),
            key = "custom-key-1",
          ),
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureB"),
            listOf(FeatureFlagOption("TwoA", isDefault = true), FeatureFlagOption("TwoB")),
            trueOption = FeatureFlagTrueOption("TwoA"),
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
        import kotlin.Boolean
        import kotlin.String

        internal fun OptionFactory.Companion.generated(): OptionFactory = Factory

        private object Factory : OptionFactory {
          override fun create(key: String, name: String): Feature<*>? = when (key) {
            "custom-key-1" -> when (name) {
              "OneA" -> FeatureA.OneA
              "OneB" -> FeatureA.OneB
              else -> null
            }
            "custom-key-2" -> when (name) {
              "TwoA" -> FeatureB.TwoA
              "TwoB" -> FeatureB.TwoB
              else -> null
            }
            else -> null
          }

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = when (key) {
            "custom-key-1" -> if (binaryValue) FeatureA.OneA else FeatureA.OneB
            "custom-key-2" -> if (binaryValue) FeatureB.TwoA else FeatureB.TwoB
            else -> null
          }
        }
        """
  }

  @Test
  fun `duplicate keys`() {
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

  @Test
  fun `feature with key matching self fqcn`() {
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
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = null
        }
        """
  }

  @Test
  fun `binary feature with key matching self fqcn`() {
    val model =
      OptionFactoryModel(
        ClassName("io.mehow", "Factory"),
        listOf(
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("A", isDefault = true), FeatureFlagOption("B")),
            trueOption = FeatureFlagTrueOption("A"),
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
        import kotlin.Boolean
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

          override fun create(key: String, binaryValue: Boolean): Feature<*>? = when (key) {
            "io.mehow.FeatureA" -> if (binaryValue) FeatureA.A else FeatureA.B
            else -> null
          }
        }
        """
  }

  @Test
  fun `key matching other fqcn`() {
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
}
