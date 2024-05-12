package io.mehow.laboratory.inspector

import app.cash.turbine.test
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

@Suppress("UNCHECKED_CAST")
class InspectorViewModelNavigationSpec : FunSpec({
  setMainDispatcher()

  test("finds coordinates for registered feature flags") {
    val viewModel = InspectorViewModel()

    viewModel.goTo(SectionOneFeatureA::class.java as Class<Feature<*>>) shouldBe FeatureCoordinates(0, 0)
    viewModel.goTo(SectionOneFeatureB::class.java as Class<Feature<*>>) shouldBe FeatureCoordinates(0, 1)
    viewModel.goTo(SectionTwoFeature::class.java as Class<Feature<*>>) shouldBe FeatureCoordinates(1, 0)
  }

  test("does not find coordinates for unregistered feature flags") {
    val viewModel = InspectorViewModel()

    viewModel.goTo(UnregisteredFeature::class.java as Class<Feature<*>>) shouldBe null
  }

  test("does not find coordinates for filtered feature flags") {
    val viewModel = InspectorViewModel(searchFlow = flowOf(SearchQuery("Foo")))

    viewModel.goTo(SectionOneFeatureA::class.java as Class<Feature<*>>) shouldBe null
  }

  test("finds multiple coordinates for a feature registered twice") {
    val viewModel = InspectorViewModel(mapOf("A1" to SectionAFactory, "A2" to SectionAFactory))

    viewModel.goTo(SectionOneFeatureA::class.java as Class<Feature<*>>) shouldBeIn listOf(
      FeatureCoordinates(0, 0),
      FeatureCoordinates(1, 0),
    )
  }

  test("emits coordinates") {
    val viewModel = InspectorViewModel()

    viewModel.featureCoordinatesFlow.test {
      expectNoEvents()

      viewModel.goTo(SectionOneFeatureA::class.java as Class<Feature<*>>)
      awaitItem() shouldBe FeatureCoordinates(0, 0)

      cancel()
    }
  }

  test("does not cache emitted coordinates") {
    val viewModel = InspectorViewModel()

    viewModel.goTo(SectionOneFeatureA::class.java as Class<Feature<*>>)

    viewModel.featureCoordinatesFlow.test {
      cancel()
    }
  }
})

private object SectionAFactory : FeatureFactory {
  override fun create(): Set<Class<out Feature<*>>> = setOf(
    SectionOneFeatureA::class.java,
    SectionOneFeatureB::class.java,
  )
}

private object SectionBFactory : FeatureFactory {
  override fun create(): Set<Class<out Feature<*>>> = setOf(SectionTwoFeature::class.java)
}

private enum class SectionOneFeatureA : Feature<SectionOneFeatureA> {
  Option,
  ;

  override val defaultOption: SectionOneFeatureA
    get() = Option
}

private enum class SectionOneFeatureB : Feature<SectionOneFeatureB> {
  Option,
  ;

  override val defaultOption: SectionOneFeatureB
    get() = Option
}

private enum class SectionTwoFeature : Feature<SectionTwoFeature> {
  Option,
  ;

  override val defaultOption: SectionTwoFeature
    get() = Option
}

private enum class UnregisteredFeature : Feature<UnregisteredFeature> {
  Option,
  ;

  override val defaultOption: UnregisteredFeature
    get() = Option
}

private fun InspectorViewModel(
  featureFactories: Map<String, FeatureFactory> = mapOf("A" to SectionAFactory, "B" to SectionBFactory),
  searchFlow: Flow<SearchQuery> = emptyFlow(),
) = InspectorViewModel(
  Laboratory.inMemory(),
  searchFlow,
  featureFactories,
  DeprecationHandler({ fail("Unexpected call") }, { fail("Unexpected call") }),
  Dispatchers.Unconfined,
)
