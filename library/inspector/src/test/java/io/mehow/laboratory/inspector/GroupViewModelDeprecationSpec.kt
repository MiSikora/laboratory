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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.WARNING

internal class GroupViewModelDeprecationSpec : DescribeSpec({
  setMainDispatcher()

  describe("deprecated feature flags") {
    it("can be filtered out") {
      val viewModel = GroupViewModel(DeprecationHandler(
          phenotypeSelector = { Hide },
          alignmentSelector = { Regular },
      ))

      val featureNames = viewModel.observeFeatureGroup().first().map(FeatureUiModel::name)

      featureNames shouldContainExactly listOf("NotDeprecated")
    }

    it("can be struck through") {
      val viewModel = GroupViewModel(DeprecationHandler(
          phenotypeSelector = { Strikethrough },
          alignmentSelector = { Regular },
      ))

      val featureNames = viewModel.observeFeatureGroup().first().map { it.name to it.deprecationPhenotype }

      featureNames shouldContainExactly listOf(
          "DeprecatedError" to Strikethrough,
          "DeprecatedWarning" to Strikethrough,
          "NotDeprecated" to null,
      )
    }

    it("can be shown") {
      val viewModel = GroupViewModel(DeprecationHandler(
          phenotypeSelector = { Show },
          alignmentSelector = { Regular },
      ))

      val featureNames = viewModel.observeFeatureGroup().first().map { it.name to it.deprecationPhenotype }

      featureNames shouldContainExactly listOf(
          "DeprecatedError" to Show,
          "DeprecatedWarning" to Show,
          "NotDeprecated" to null,
      )
    }

    it("can be moved to bottom") {
      val viewModel = GroupViewModel(DeprecationHandler(
          phenotypeSelector = { Show },
          alignmentSelector = { Bottom },
      ))

      val featureNames = viewModel.observeFeatureGroup().first().map { it.name to it.deprecationPhenotype }

      featureNames shouldContainExactly listOf(
          "NotDeprecated" to null,
          "DeprecatedError" to Show,
          "DeprecatedWarning" to Show,
      )
    }

    it("can be selected based on deprecation level") {
      val viewModel = GroupViewModel(DeprecationHandler(
          phenotypeSelector = { if (it == WARNING) Strikethrough else Show },
          alignmentSelector = { if (it == ERROR) Bottom else Regular },
      ))

      val featureNames = viewModel.observeFeatureGroup().first().map { it.name to it.deprecationPhenotype }

      featureNames shouldContainExactly listOf(
          "DeprecatedWarning" to Strikethrough,
          "NotDeprecated" to null,
          "DeprecatedError" to Show,
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
        /*
         * https://github.com/MiSikora/laboratory/issues/62
         *
         * Class.forName("io.mehow.laboratory.inspector.DeprecatedHidden"),
         */
        NotDeprecated::class.java
    ) as Set<Class<Feature<*>>>
  }
}

@Deprecated("", level = WARNING)
private enum class DeprecatedWarning : Feature<@Suppress("DEPRECATION") DeprecatedWarning> {
  Option,
  ;

  override val defaultOption get() = Option
}

@Deprecated("message", level = ERROR)
private enum class DeprecatedError : Feature<@Suppress("DEPRECATION_ERROR") DeprecatedError> {
  Option,
  ;

  override val defaultOption get() = Option
}

/*
 * https://github.com/MiSikora/laboratory/issues/62
 *
 * @Deprecated("", level = HIDDEN)
 * private enum class DeprecatedHidden : Feature<DeprecatedHidden> {
 *   Option,
 *   ;
 *
 *   override val defaultOption get() = Option
 * }
 */

private enum class NotDeprecated : Feature<NotDeprecated> {
  Option,
  ;

  override val defaultOption get() = Option
}

@Suppress("TestFunctionName")
private fun GroupViewModel(
  deprecationHandler: DeprecationHandler,
) = GroupViewModel(
    Laboratory.inMemory(),
    DeprecatedFeatureFactory,
    deprecationHandler,
    emptyFlow(),
)
