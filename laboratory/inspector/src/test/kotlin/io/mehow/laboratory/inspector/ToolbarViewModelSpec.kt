package io.mehow.laboratory.inspector

import app.cash.turbine.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.coroutines.backgroundScope
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import io.mehow.laboratory.testing.FeatureC
import io.mehow.laboratory.testing.perTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.yield

@OptIn(ExperimentalCoroutinesApi::class)
class ToolbarViewModelSpec : FunSpec() {
  init {
    coroutineTestScope = true

    val laboratory by perTest { Laboratory.inMemory() }

    test("search interaction") {
      val viewModel = ToolbarViewModel(laboratory, backgroundScope)

      viewModel.uiModels.test {
        var uiModel = awaitItem()
        uiModel.isSearchOpen.shouldBeFalse()
        uiModel.query shouldBe QueryString.Empty

        // Search mode is not enabled
        uiModel.updateQuery("Hello")
        expectNoEvents()

        uiModel.openSearch()
        uiModel = awaitItem()
        uiModel.isSearchOpen.shouldBeTrue()

        uiModel.updateQuery("abc")
        uiModel = awaitItem()
        uiModel.query shouldBe QueryString.create("abc")

        uiModel.updateQuery("def")
        uiModel = awaitItem()
        uiModel.query shouldBe QueryString.create("def")

        uiModel.closeSearch()
        uiModel = awaitItem()
        uiModel.isSearchOpen.shouldBeFalse()
        uiModel.query shouldBe QueryString.Empty
      }
    }

    test("clear laboratory") {
      val viewModel = ToolbarViewModel(laboratory, backgroundScope)

      laboratory.setOptions(FeatureA.C, FeatureB.A, FeatureC.B)
      viewModel.uiModels.value.resetFeatureFlags()
      yield()

      laboratory.experiment<FeatureA>() shouldBe FeatureA.A
      laboratory.experiment<FeatureB>() shouldBe FeatureB.B
      laboratory.experiment<FeatureC>() shouldBe FeatureC.C
    }
  }
}
