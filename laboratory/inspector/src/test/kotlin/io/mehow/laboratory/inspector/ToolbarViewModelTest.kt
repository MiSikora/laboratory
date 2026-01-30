package io.mehow.laboratory.inspector

import app.cash.turbine.test
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.testing.FeatureA
import io.mehow.laboratory.testing.FeatureB
import io.mehow.laboratory.testing.FeatureC
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Test

class ToolbarViewModelTest {
  val scope = TestScope()

  private val laboratory = Laboratory.inMemory()

  private val viewModel = ToolbarViewModel(laboratory, scope.backgroundScope)

  @Test
  fun `search interaction`() =
    scope.runTest {
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

  @Test
  fun `clear laboratory`() =
    scope.runTest {
      laboratory.setOptions(FeatureA.C, FeatureB.A, FeatureC.B)
      viewModel.uiModels.value.resetFeatureFlags()
      yield()

      laboratory.experiment<FeatureA>() shouldBe FeatureA.A
      laboratory.experiment<FeatureB>() shouldBe FeatureB.B
      laboratory.experiment<FeatureC>() shouldBe FeatureC.C
    }
}
