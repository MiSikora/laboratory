package io.mehow.laboratory.inspector

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.inspector.SearchMode.Active
import io.mehow.laboratory.inspector.SearchMode.Idle
import io.mehow.laboratory.inspector.SearchViewModel.Event.CloseSearch
import io.mehow.laboratory.inspector.SearchViewModel.Event.OpenSearch
import io.mehow.laboratory.inspector.SearchViewModel.Event.UpdateQuery
import io.mehow.laboratory.inspector.SearchViewModel.UiModel

internal class SearchViewModelSpec : DescribeSpec({
  setMainDispatcher()

  describe("feature flag searching") {
    it("has initial idle state") {
      SearchViewModel().uiModels.test {
        expectIdleModel()

        cancel()
      }
    }

    it("can be opened") {
      val viewModel = SearchViewModel()
      viewModel.uiModels.test {
        expectIdleModel()

        viewModel.sendEvent(OpenSearch)
        awaitItem() shouldBe UiModel(Active, SearchQuery.Empty)

        cancel()
      }
    }

    it("updates search queries in active mode") {
      val viewModel = SearchViewModel()
      viewModel.uiModels.test {
        expectIdleModel()

        viewModel.sendEvent(OpenSearch)
        awaitItem()

        viewModel.sendEvent(UpdateQuery("Hello"))
        awaitItem() shouldBe UiModel(Active, SearchQuery("Hello"))

        viewModel.sendEvent(UpdateQuery("World"))
        awaitItem() shouldBe UiModel(Active, SearchQuery("World"))

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

    it("clears queries when search is closed") {
      val viewModel = SearchViewModel()
      viewModel.uiModels.test {
        expectIdleModel()

        viewModel.sendEvent(OpenSearch)
        awaitItem()

        viewModel.sendEvent(UpdateQuery("Hello"))
        awaitItem() shouldBe UiModel(Active, SearchQuery("Hello"))

        viewModel.sendEvent(CloseSearch)
        expectIdleModel()

        cancel()
      }
    }
  }
})

private suspend fun FlowTurbine<UiModel>.expectIdleModel() {
  awaitItem() shouldBe UiModel(Idle, SearchQuery.Empty)
}
