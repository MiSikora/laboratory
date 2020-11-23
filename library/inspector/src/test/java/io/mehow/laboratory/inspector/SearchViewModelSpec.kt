package io.mehow.laboratory.inspector

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.inspector.SearchMode.Active
import io.mehow.laboratory.inspector.SearchMode.Idle
import io.mehow.laboratory.inspector.SearchQuery.Companion
import io.mehow.laboratory.inspector.SearchViewModel.Event.ToggleSearchMode
import io.mehow.laboratory.inspector.SearchViewModel.Event.UpdateQuery
import io.mehow.laboratory.inspector.SearchViewModel.SearchUiModel

internal class SearchViewModelSpec : DescribeSpec({
  setMainDispatcher()

  describe("feature flag searching") {
    it("has initial idle state") {
      SearchViewModel().uiModels.test {
        expectIdleModel()

        cancel()
      }
    }

    it("can be toggled") {
      val viewModel = SearchViewModel()
      viewModel.uiModels.test {
        expectIdleModel()

        viewModel.sendEvent(ToggleSearchMode)
        expectItem() shouldBe SearchUiModel(Active, SearchQuery.Empty)

        viewModel.sendEvent(ToggleSearchMode)
        expectItem() shouldBe SearchUiModel(Idle, Companion.Empty)

        cancel()
      }
    }

    it("updates search queries in active mode") {
      val viewModel = SearchViewModel()
      viewModel.uiModels.test {
        expectIdleModel()

        viewModel.sendEvent(ToggleSearchMode)
        expectItem()

        viewModel.sendEvent(UpdateQuery("Hello"))
        expectItem() shouldBe SearchUiModel(Active, SearchQuery("Hello"))

        viewModel.sendEvent(UpdateQuery("World"))
        expectItem() shouldBe SearchUiModel(Active, SearchQuery("World"))

        cancel()
      }
    }

    it("ignores queries in idle mode") {
      val viewModel = SearchViewModel()
      viewModel.uiModels.test {
        expectIdleModel()

        viewModel.sendEvent(UpdateQuery("Hello"))
        expectNoEvents()

        cancel()
      }
    }

    it("clears queries when toggled to idle mode") {
      val viewModel = SearchViewModel()
      viewModel.uiModels.test {
        expectIdleModel()

        viewModel.sendEvent(ToggleSearchMode)
        expectItem()

        viewModel.sendEvent(UpdateQuery("Hello"))
        expectItem() shouldBe SearchUiModel(Active, SearchQuery("Hello"))

        viewModel.sendEvent(ToggleSearchMode)
        expectIdleModel()

        cancel()
      }
    }
  }
})

private suspend fun FlowTurbine<SearchUiModel>.expectIdleModel() {
  expectItem() shouldBe SearchUiModel(Idle, SearchQuery.Empty)
}
