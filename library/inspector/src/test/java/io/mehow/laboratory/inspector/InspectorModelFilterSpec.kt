package io.mehow.laboratory.inspector

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.property.Arb
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.checkAll
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.reflect.KClass

internal class InspectorViewModelFilterSpec : DescribeSpec({
  setMainDispatcher()

  describe("feature flags filtering") {
    it("emits all feature flags for no search terms") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        cancel()
      }
    }

    it("emits all feature flags for blank search terms") {
      checkAll(Arb.stringPattern("([ ]{0,10})")) { query ->
        val searchFlow = MutableSharedFlow<SearchQuery>()
        InspectorViewModel(searchFlow).observeFeatureClasses().test {
          expectAllFeatureFlags()

          searchFlow.emit(SearchQuery(query))
          expectNoEvents()

          cancel()
        }
      }
    }

    it("finds feature flags by their exact name") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("RegularNameFeature"))
        expectItem() shouldContainExactly listOf(RegularNameFeature::class)

        searchFlow.emit(SearchQuery("Numbered1NameFeature"))
        expectItem() shouldContainExactly listOf(Numbered1NameFeature::class)

        searchFlow.emit(SearchQuery("SourcedFeature"))
        expectItem() shouldContainExactly listOf(SourcedFeature::class)

        cancel()
      }
    }

    it("finds feature flags by split name parts") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("Name Feature"))
        expectItem() shouldContainExactly listOf(Numbered1NameFeature::class, RegularNameFeature::class)

        searchFlow.emit(SearchQuery("Numbered 1Name Feature"))
        expectItem() shouldContainExactly listOf(Numbered1NameFeature::class)

        cancel()
      }
    }

    // Find out why checkAll(Arb.stringPattern("[!_?@*]{10,}")) { query -> } can fail on CI.
    // It fails randomly with a timeout on a second event. Re-using generator seed does not help locally.
    // Pattern in generator also doesn't matter as long as it produces valid input for the test.
    it("finds no feature flags for no matches") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("???"))
        expectItem() shouldContainExactly emptyList()

        cancel()
      }
    }

    it("finds feature flags by their options") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("Disabled"))
        expectItem() shouldContainExactly listOf(Numbered1NameFeature::class, RegularNameFeature::class)

        searchFlow.emit(SearchQuery("Howdy"))
        expectItem() shouldContainExactly listOf(SourcedFeature::class)

        cancel()
      }
    }

    it("finds feature flags by their sources") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("Remote"))
        expectItem() shouldContainExactly listOf(SourcedFeature::class)

        cancel()
      }
    }

    it("finds feature flags by partial matches") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("me ture"))
        expectItem() shouldContainExactly listOf(Numbered1NameFeature::class, RegularNameFeature::class)

        searchFlow.emit(SearchQuery("ature"))
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("ed ture"))
        expectItem() shouldContainExactly listOf(Numbered1NameFeature::class, SourcedFeature::class)

        searchFlow.emit(SearchQuery("cal"))
        expectItem() shouldContainExactly listOf(SourcedFeature::class)

        cancel()
      }
    }

    it("finds feature flags by ordered input") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("Enabled Disabled"))
        expectItem() shouldContainExactly listOf(RegularNameFeature::class)

        searchFlow.emit(SearchQuery("Disabled Enabled"))
        expectItem() shouldContainExactly listOf(Numbered1NameFeature::class)

        cancel()
      }
    }

    it("ignores capitalization during search") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("enabled"))
        expectItem() shouldContainExactly listOf(Numbered1NameFeature::class, RegularNameFeature::class)

        searchFlow.emit(SearchQuery("feature"))
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("local"))
        expectItem() shouldContainExactly listOf(SourcedFeature::class)

        cancel()
      }
    }

    it("finds feature flags by partial, non-split inner search") {
      val searchFlow = MutableSharedFlow<SearchQuery>()
      InspectorViewModel(searchFlow).observeFeatureClasses().test {
        expectAllFeatureFlags()

        searchFlow.emit(SearchQuery("arnamefea"))
        expectItem() shouldContainExactly listOf(RegularNameFeature::class)

        searchFlow.emit(SearchQuery("d1na"))
        expectItem() shouldContainExactly listOf(Numbered1NameFeature::class)

        cancel()
      }
    }
  }
})

private object SearchFeatureFactory : FeatureFactory {
  @Suppress("UNCHECKED_CAST")
  override fun create(): Set<Class<Feature<*>>> = setOf(
      RegularNameFeature::class.java,
      Numbered1NameFeature::class.java,
      SourcedFeature::class.java,
  ) as Set<Class<Feature<*>>>
}

private enum class RegularNameFeature : Feature<RegularNameFeature> {
  Enabled,
  Disabled,
  ;

  override val defaultOption get() = Disabled
}

private enum class Numbered1NameFeature : Feature<Numbered1NameFeature> {
  Disabled,
  Enabled,
  ;

  override val defaultOption get() = Disabled
}

private enum class SourcedFeature : Feature<SourcedFeature> {
  Howdy,
  There,
  Partner,
  ;

  override val defaultOption get() = Howdy

  @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  enum class Source : Feature<Source> {
    Local,
    Remote,
    ;

    override val defaultOption get() = Local
  }
}

@Suppress("TestFunctionName")
private fun InspectorViewModel(
  searchFlow: Flow<SearchQuery>,
) = InspectorViewModel(
    Laboratory.inMemory(),
    searchFlow,
    SearchFeatureFactory,
    DeprecationHandler({ fail("Unexpected call") }, { fail("Unexpected call") }),
)

private suspend fun FlowTurbine<List<KClass<out Feature<*>>>>.expectAllFeatureFlags() {
  expectItem() shouldContainExactly listOf(
      Numbered1NameFeature::class,
      RegularNameFeature::class,
      SourcedFeature::class,
  )
}
