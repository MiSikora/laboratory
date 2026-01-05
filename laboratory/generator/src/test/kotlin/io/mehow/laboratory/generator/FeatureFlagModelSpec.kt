package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.generator.test.shouldSpecify
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.HIDDEN
import kotlin.DeprecationLevel.WARNING

class FeatureFlagModelSpec : FunSpec() {
  init {
    test("public visibility") {
      val model =
        FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("A", isDefault = true)),
          visibility = Public,
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature

        public enum class FeatureA : Feature<FeatureA> {
          A,
          ;

          override val defaultOption: FeatureA
            get() = A
        }
        """
    }

    test("internal visibility") {
      val model =
        FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("A", isDefault = true)),
          visibility = Internal,
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature

        internal enum class FeatureA : Feature<FeatureA> {
          A,
          ;

          override val defaultOption: FeatureA
            get() = A
        }
        """
    }

    test("single option") {
      val model =
        FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("A", isDefault = true)),
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature

        public enum class FeatureA : Feature<FeatureA> {
          A,
          ;

          override val defaultOption: FeatureA
            get() = A
        }
        """
    }

    test("multiple option") {
      val model =
        FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("A"), FeatureFlagOption("B", isDefault = true)),
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature

        public enum class FeatureA : Feature<FeatureA> {
          A,
          B,
          ;

          override val defaultOption: FeatureA
            get() = B
        }
        """
    }

    test("no options") {
      val exception =
        shouldThrow<IllegalArgumentException> {
          FeatureFlagModel(ClassName("io.mehow", "FeatureA"), emptyList())
        }

      exception shouldHaveMessage "io.mehow.FeatureA must have at least one option"
    }

    test("no default option") {
      val exception =
        shouldThrow<IllegalArgumentException> {
          FeatureFlagModel(ClassName("io.mehow", "FeatureA"), listOf(FeatureFlagOption("A")))
        }

      exception shouldHaveMessage "io.mehow.FeatureA must have exactly one default option"
    }

    test("fails to generate when there are multiple default options") {
      val exception =
        shouldThrow<IllegalArgumentException> {
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(
              FeatureFlagOption("A", isDefault = true),
              FeatureFlagOption("B", isDefault = true),
            ),
          )
        }

      exception shouldHaveMessage "io.mehow.FeatureA must have exactly one default option"
    }

    test("local source") {
      val model =
        FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("A", isDefault = true)),
          isRemote = false,
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature

        public enum class FeatureA : Feature<FeatureA> {
          A,
          ;

          override val defaultOption: FeatureA
            get() = A
        }
        """
    }

    test("remote source") {
      val model =
        FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("A", isDefault = true)),
          isRemote = true,
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature

        public enum class FeatureA : Feature<FeatureA> {
          A,
          ;

          override val defaultOption: FeatureA
            get() = A

          override val defaultSource: Feature.Source
            get() = Feature.Source.Remote
        }
        """
    }

    test("description") {
      val model =
        FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("A", isDefault = true)),
          description = "Feature description",
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import kotlin.String

        /**
         * Feature description
         */
        public enum class FeatureA : Feature<FeatureA> {
          A,
          ;

          override val defaultOption: FeatureA
            get() = A

          override val description: String = "Feature description"
        }
        """
    }

    test("description with long hyperlink") {
      val model =
        FeatureFlagModel(
          ClassName("io.mehow", "FeatureA"),
          listOf(FeatureFlagOption("A", isDefault = true)),
          description =
            "Some [long hyperlink](https://square.github.io/kotlinpoet/1.x/kotlinpoet-classinspector-elements/com.squareup.kotlinpoet.classinspector.elements/) in the description.",
        )

      val fileSpec = model.prepare()

      fileSpec shouldSpecify
        """
        package io.mehow

        import io.mehow.laboratory.Feature
        import kotlin.String

        /**
         * Some [long hyperlink](https://square.github.io/kotlinpoet/1.x/kotlinpoet-classinspector-elements/com.squareup.kotlinpoet.classinspector.elements/) in the description.
         */
        public enum class FeatureA : Feature<FeatureA> {
          A,
          ;

          override val defaultOption: FeatureA
            get() = A

          override val description: String =
              "Some [long hyperlink](https://square.github.io/kotlinpoet/1.x/kotlinpoet-classinspector-elements/com.squareup.kotlinpoet.classinspector.elements/) in the description."
        }
        """
    }

    enumValues<DeprecationLevel>().forEach { level ->
      test("$level deprecation") {
        val model =
          FeatureFlagModel(
            ClassName("io.mehow", "FeatureA"),
            listOf(FeatureFlagOption("A", isDefault = true)),
            deprecation = Deprecation("Deprecation message", level),
          )
        val suppressLevel =
          when (level) {
            WARNING -> "DEPRECATION"
            ERROR,
            HIDDEN -> "DEPRECATION_ERROR"
          }

        val fileSpec = model.prepare()

        fileSpec shouldSpecify
          """
          package io.mehow

          import io.mehow.laboratory.Feature
          import kotlin.Deprecated
          import kotlin.DeprecationLevel
          import kotlin.Suppress

          @Deprecated(
            message = "Deprecation message",
            level = DeprecationLevel.$level,
          )
          public enum class FeatureA : Feature<@Suppress("$suppressLevel") FeatureA> {
            A,
            ;

            @Suppress("$suppressLevel")
            override val defaultOption: FeatureA
              get() = A
          }
          """
      }
    }
  }
}
