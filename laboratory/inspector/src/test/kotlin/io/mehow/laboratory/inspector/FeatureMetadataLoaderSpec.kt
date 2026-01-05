@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package io.mehow.laboratory.inspector

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.test.DeprecatedErrorFeature
import io.mehow.laboratory.inspector.test.DeprecatedWarningFeature
import io.mehow.laboratory.inspector.test.FeatureWithDescription
import io.mehow.laboratory.inspector.test.LocalFeature
import io.mehow.laboratory.inspector.test.NoOpDeprecationHandler
import io.mehow.laboratory.inspector.test.RemoteFeature
import io.mehow.laboratory.inspector.test.TestFeatureFactory
import io.mehow.laboratory.testing.options
import io.mehow.laboratory.testing.shouldHaveSingle
import kotlin.reflect.KClass

class FeatureMetadataLoaderSpec : FunSpec() {
  init {
    val loader = FeatureMetadata.Loader(TestFeatureFactory, NoOpDeprecationHandler)

    test("local feature") {
      val features = loader.load()

      val feature = features.shouldHaveSingle { it.type == LocalFeature::class.java }

      feature.name shouldBe "LocalFeature"
      feature.options shouldContainExactly LocalFeature::class.options
      feature.defaultSource shouldBe Feature.Source.Local
    }

    test("remote feature") {
      val features = loader.load()

      val feature = features.shouldHaveSingle { it.type == RemoteFeature::class.java }

      feature.name shouldBe "RemoteFeature"
      feature.options shouldContainExactly RemoteFeature::class.options
      feature.defaultSource shouldBe Feature.Source.Remote
    }

    test("feature with description") {
      val features = loader.load()

      val feature = features.shouldHaveSingle { it.type == FeatureWithDescription::class.java }

      feature.name shouldBe "FeatureWithDescription"
      feature.options shouldContainExactly FeatureWithDescription::class.options
      feature.defaultSource shouldBe Feature.Source.Local
      feature.description shouldBe
        listOf(
          TextToken.Regular("Description with a "),
          TextToken.Link("link", "https://mehow.io/laboratory/"),
        )
    }

    test("warning deprecated feature") {
      val features = loader.load()

      val feature = features.shouldHaveSingle { it.type == DeprecatedWarningFeature::class.java }

      feature.name shouldBe "DeprecatedWarningFeature"
      feature.options shouldContainExactly DeprecatedWarningFeature::class.options
      feature.defaultSource shouldBe Feature.Source.Local
    }

    test("error deprecated feature") {
      val features = loader.load()

      val feature = features.shouldHaveSingle { it.type == DeprecatedErrorFeature::class.java }

      feature.name shouldBe "DeprecatedErrorFeature"
      feature.options shouldContainExactly DeprecatedErrorFeature::class.options
      feature.defaultSource shouldBe Feature.Source.Local
    }

    test("ids") {
      val featureIndices = loader.load().map(FeatureMetadata::id)

      featureIndices.shouldBeUnique()
    }

    test("alphabetical sorting") {
      val features = loader.load()

      features shouldBeSortedBy FeatureMetadata::name
    }

    test("deprecation style") {
      val loader =
        FeatureMetadata.Loader(
          TestFeatureFactory,
          DeprecationHandler(
            styleSelector = { level ->
              if (level == DeprecationLevel.WARNING) {
                FeatureStyle.Strikethrough
              } else {
                FeatureStyle.Hide
              }
            },
            alignmentSelector = { FeatureAlignment.Regular },
          ),
        )

      val features = loader.load()

      features[LocalFeature::class]?.style shouldBe FeatureStyle.Show
      features[RemoteFeature::class]?.style shouldBe FeatureStyle.Show
      features[FeatureWithDescription::class]?.style shouldBe FeatureStyle.Show
      features[DeprecatedWarningFeature::class]?.style shouldBe FeatureStyle.Strikethrough
      features[DeprecatedErrorFeature::class]?.style.shouldBeNull()
    }

    test("deprecation sorting") {
      val loader =
        FeatureMetadata.Loader(
          TestFeatureFactory,
          DeprecationHandler(
            styleSelector = { FeatureStyle.Show },
            alignmentSelector = { level ->
              if (level == DeprecationLevel.WARNING) {
                FeatureAlignment.Bottom
              } else {
                FeatureAlignment.Regular
              }
            },
          ),
        )

      val features = loader.load().map(FeatureMetadata::type)

      features shouldContainExactly
        listOf(
          DeprecatedErrorFeature::class.java,
          FeatureWithDescription::class.java,
          LocalFeature::class.java,
          RemoteFeature::class.java,
          DeprecatedWarningFeature::class.java,
        )
    }
  }
}

private operator fun List<FeatureMetadata>.get(feature: KClass<out Feature<*>>) = find {
  it.type == feature.java
}
