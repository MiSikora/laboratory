package io.mehow.laboratory.inspector

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.DeprecationAlignment.Bottom
import io.mehow.laboratory.inspector.DeprecationAlignment.Regular
import io.mehow.laboratory.inspector.DeprecationPhenotype.Hide
import io.mehow.laboratory.inspector.DeprecationPhenotype.Show
import io.mehow.laboratory.inspector.DeprecationPhenotype.Strikethrough
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.HIDDEN
import kotlin.DeprecationLevel.WARNING

internal class InspectorViewModelDeprecationSpec : DescribeSpec({
  setMainDispatcher()

  describe("deprecated feature flags") {
    it("can be filtered out") {
      val viewModel = InspectorViewModel(DeprecationHandler(
          phenotypeSelector = { Hide },
          alignmentSelector = { Regular },
      ))

      val featureNames = viewModel.sectionFlow().first().map(FeatureUiModel::name)

      featureNames shouldContainExactly listOf("NotDeprecated")
    }

    it("can be struck through") {
      val viewModel = InspectorViewModel(DeprecationHandler(
          phenotypeSelector = { Strikethrough },
          alignmentSelector = { Regular },
      ))

      val featureNames = viewModel.sectionFlow().first().map { it.name to it.deprecationPhenotype }

      featureNames shouldContainExactly listOf(
          "DeprecatedError" to Strikethrough,
          "DeprecatedHidden" to Strikethrough,
          "DeprecatedWarning" to Strikethrough,
          "NotDeprecated" to null,
      )
    }

    it("can be shown") {
      val viewModel = InspectorViewModel(DeprecationHandler(
          phenotypeSelector = { Show },
          alignmentSelector = { Regular },
      ))

      val featureNames = viewModel.sectionFlow().first().map { it.name to it.deprecationPhenotype }

      featureNames shouldContainExactly listOf(
          "DeprecatedError" to Show,
          "DeprecatedHidden" to Show,
          "DeprecatedWarning" to Show,
          "NotDeprecated" to null,
      )
    }

    it("can be moved to bottom") {
      val viewModel = InspectorViewModel(DeprecationHandler(
          phenotypeSelector = { Show },
          alignmentSelector = { Bottom },
      ))

      val featureNames = viewModel.sectionFlow().first().map { it.name to it.deprecationPhenotype }

      featureNames shouldContainExactly listOf(
          "NotDeprecated" to null,
          "DeprecatedError" to Show,
          "DeprecatedHidden" to Show,
          "DeprecatedWarning" to Show,
      )
    }

    it("can be selected based on deprecation level") {
      val viewModel = InspectorViewModel(DeprecationHandler(
          phenotypeSelector = { if (it == WARNING) Strikethrough else Show },
          alignmentSelector = { if (it != WARNING) Bottom else Regular },
      ))

      val featureNames = viewModel.sectionFlow().first().map { it.name to it.deprecationPhenotype }

      featureNames shouldContainExactly listOf(
          "DeprecatedWarning" to Strikethrough,
          "NotDeprecated" to null,
          "DeprecatedError" to Show,
          "DeprecatedHidden" to Show,
      )
    }
  }
})

private object DeprecatedFeatureFactory : FeatureFactory {
  override fun create(): Set<Class<Feature<*>>> {
    @Suppress("UNCHECKED_CAST")
    return setOf(
        Class.forName("io.mehow.laboratory.inspector.DeprecatedWarning"),
        Class.forName("io.mehow.laboratory.inspector.DeprecatedError"),
        Class.forName("io.mehow.laboratory.inspector.DeprecatedHidden"),
        Class.forName("io.mehow.laboratory.inspector.NotDeprecated"),
    ) as Set<Class<Feature<*>>>
  }
}

@Deprecated("", level = WARNING)
private enum class DeprecatedWarning : Feature<@Suppress("DEPRECATION") DeprecatedWarning> {
  Option,
  ;

  @Suppress("DEPRECATION")
  override val defaultOption: DeprecatedWarning
    get() = Option
}

@Deprecated("message", level = ERROR)
private enum class DeprecatedError : Feature<@Suppress("DEPRECATION_ERROR") DeprecatedError> {
  Option,
  ;

  @Suppress("DEPRECATION_ERROR")
  override val defaultOption: DeprecatedError
    get() = Option
}

@Deprecated("", level = HIDDEN)
private enum class DeprecatedHidden : Feature<@Suppress("DEPRECATION_ERROR") DeprecatedHidden> {
  Option,
  ;

  @Suppress("DEPRECATION_ERROR")
  override val defaultOption: DeprecatedHidden
    get() = Option
}

private enum class NotDeprecated : Feature<NotDeprecated> {
  Option,
  ;

  override val defaultOption: NotDeprecated
    get() = Option
}

@Suppress("TestFunctionName")
private fun InspectorViewModel(
  deprecationHandler: DeprecationHandler,
) = InspectorViewModel(
    Laboratory.inMemory(),
    emptyFlow(),
    DeprecatedFeatureFactory,
    deprecationHandler,
)
