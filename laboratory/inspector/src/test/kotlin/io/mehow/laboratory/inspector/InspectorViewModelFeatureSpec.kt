package io.mehow.laboratory.inspector

import app.cash.turbine.test
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.mehow.laboratory.DefaultOptionFactory
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.TextToken.Link
import io.mehow.laboratory.inspector.TextToken.Regular
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first

class InspectorViewModelFeatureSpec :
  FunSpec({
    setMainDispatcher()

    test("filters empty feature flag groups") {
      val viewModel = InspectorViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      val featureNames = viewModel.sectionFlow().first().map(FeatureUiModel::name)

      featureNames shouldNotContain "Empty"
    }

    test("orders feature flag groups by name") {
      val viewModel = InspectorViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      val featureNames = viewModel.sectionFlow().first().map(FeatureUiModel::name)

      featureNames shouldContainExactly listOf("First", "Second")
    }

    test("does not order feature flag options") {
      val viewModel = InspectorViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      val features =
        viewModel.sectionFlow().first().map(FeatureUiModel::models).map { models ->
          models.map(OptionUiModel::option)
        }

      features[0] shouldContainExactly listOf(First.C, First.B, First.A)
      features[1] shouldContainExactly listOf(Second.B, Second.C, Second.A)
    }

    test("marks first feature flag option as selected by default") {
      val viewModel = InspectorViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      viewModel.observeSelectedFeatures().first() shouldContainExactly listOf(First.C, Second.B)
    }

    test("marks saved feature flag options as selected") {
      val laboratory =
        Laboratory.inMemory().apply {
          setOption(First.A)
          setOption(Second.C)
        }

      val viewModel = InspectorViewModel(laboratory, NoSourceFeatureFactory)

      viewModel.observeSelectedFeatures().first() shouldContainExactly listOf(First.A, Second.C)
    }

    test("selects feature flag options") {
      val viewModel = InspectorViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      viewModel.selectFeature(First.B)
      viewModel.selectFeature(Second.A)

      viewModel.observeSelectedFeatures().first() shouldContainExactly listOf(First.B, Second.A)
    }

    test("emits feature flag changes") {
      val viewModel = InspectorViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      viewModel.observeSelectedFeatures().test {
        awaitItem() shouldContainExactly listOf(First.C, Second.B)

        viewModel.selectFeature(First.B)
        awaitItem() shouldContainExactly listOf(First.B, Second.B)

        viewModel.selectFeature(Second.C)
        awaitItem() shouldContainExactly listOf(First.B, Second.C)

        cancel()
      }
    }

    test("emits source changes") {
      val viewModel = InspectorViewModel(Laboratory.inMemory(), AllFeatureFactory)

      viewModel.observeSelectedFeaturesAndSources().test {
        awaitItem() shouldContainExactly
          listOf(First.C to null, Second.B to null, Sourced.A to Sourced.Source.Local)

        viewModel.selectFeature(Sourced.Source.Remote)

        awaitItem() shouldContainExactly
          listOf(First.C to null, Second.B to null, Sourced.A to Sourced.Source.Remote)
      }
    }

    test("resets feature flags to default options declared in factory") {
      val defaultOptionFactory =
        object : DefaultOptionFactory {
          override fun <T : Feature<out T>> create(feature: T): Feature<*>? =
            when (feature) {
              is First -> First.A
              is Second -> Second.A
              else -> null
            }
        }
      val laboratory =
        Laboratory.builder()
          .featureStorage(FeatureStorage.inMemory())
          .defaultOptionFactory(defaultOptionFactory)
          .build()
      val viewModel = InspectorViewModel(laboratory, NoSourceFeatureFactory)

      viewModel.observeSelectedFeatures().test {
        awaitItem() shouldContainExactly listOf(First.A, Second.A)

        viewModel.selectFeature(First.B)
        awaitItem() shouldContainExactly listOf(First.B, Second.A)

        viewModel.selectFeature(Second.B)
        awaitItem() shouldContainExactly listOf(First.B, Second.B)

        laboratory.clear()
        awaitItemEventually { it shouldContainExactly listOf(First.A, Second.A) }

        cancel()
      }
    }

    test("uses text tokens for feature flag description") {
      val viewModel = InspectorViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      val descriptions = viewModel.sectionFlow().first().map(FeatureUiModel::description)

      descriptions shouldContainExactly
        listOf(
          listOf(Regular("Description with a "), Link("link", "https://mehow.io")),
          listOf(Regular("Description without a link")),
        )
    }
  })

private object NoSourceFeatureFactory : FeatureFactory {
  override fun create(): Set<Class<out Feature<*>>> =
    setOf(Second::class.java, First::class.java, Empty::class.java)
}

private object SourcedFeatureFactory : FeatureFactory {
  override fun create(): Set<Class<out Feature<*>>> = setOf(Sourced::class.java)
}

private object AllFeatureFactory : FeatureFactory {
  override fun create() = NoSourceFeatureFactory.create() + SourcedFeatureFactory.create()
}

private enum class First : Feature<First> {
  C,
  B,
  A;

  override val defaultOption
    get() = C

  override val description = "Description with a [link](https://mehow.io)"
}

private enum class Second : Feature<Second> {
  B,
  C,
  A;

  override val defaultOption
    get() = B

  override val description = "Description without a link"
}

private enum class Empty : Feature<Empty>

private enum class Sourced : Feature<Sourced> {
  A,
  B,
  C;

  override val defaultOption
    get() = A

  override val source = Source::class.java

  enum class Source : Feature<Source> {
    Local,
    Remote;

    override val defaultOption
      get() = Local
  }
}

private fun InspectorViewModel(laboratory: Laboratory, factory: FeatureFactory) =
  InspectorViewModel(
    laboratory,
    emptyFlow(),
    factory,
    DeprecationHandler({ fail("Unexpected call") }, { fail("Unexpected call") }),
  )
