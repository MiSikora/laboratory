package io.mehow.laboratory.inspector

import app.cash.turbine.test
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.mehow.laboratory.DefaultOptionFactory
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class ViewModelSpec : DescribeSpec({
  describe("view model") {
    it("filters empty feature flag groups") {
      val viewModel = GroupViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      val featureNames = viewModel.observeFeatureGroups().first().map(FeatureUiModel::name)

      featureNames shouldNotContain "Empty"
    }

    it("orders feature flag groups by name") {
      val viewModel = GroupViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      val featureNames = viewModel.observeFeatureGroups().first().map(FeatureUiModel::name)

      featureNames shouldContainExactly listOf("First", "Second")
    }

    it("does not order feature flag options") {
      val viewModel = GroupViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      val features = viewModel.observeFeatureGroups().first()
          .map(FeatureUiModel::models)
          .map { models -> models.map(OptionUiModel::option) }

      features[0] shouldContainExactly listOf(First.C, First.B, First.A)
      features[1] shouldContainExactly listOf(Second.B, Second.C, Second.A)
    }

    it("marks first feature flag option as selected by default") {
      val viewModel = GroupViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      viewModel.observeSelectedFeatures().first() shouldContainExactly listOf(First.C, Second.B)
    }

    it("marks saved feature flag options as selected") {
      val laboratory = Laboratory.inMemory().apply {
        setOption(First.A)
        setOption(Second.C)
      }

      val viewModel = GroupViewModel(laboratory, NoSourceFeatureFactory)

      viewModel.observeSelectedFeatures().first() shouldContainExactly listOf(First.A, Second.C)
    }

    it("selects feature flag options") {
      val viewModel = GroupViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      viewModel.selectFeature(First.B)
      viewModel.selectFeature(Second.A)

      viewModel.observeSelectedFeatures().first() shouldContainExactly listOf(First.B, Second.A)
    }

    it("observes feature flag changes") {
      val viewModel = GroupViewModel(Laboratory.inMemory(), NoSourceFeatureFactory)

      viewModel.observeSelectedFeatures().test {
        expectItem() shouldContainExactly listOf(First.C, Second.B)

        viewModel.selectFeature(First.B)
        expectItem() shouldContainExactly listOf(First.B, Second.B)

        viewModel.selectFeature(Second.C)
        expectItem() shouldContainExactly listOf(First.B, Second.C)

        cancel()
      }
    }

    it("observes source changes") {
      val viewModel = GroupViewModel(Laboratory.inMemory(), AllFeatureFactory)

      viewModel.observeSelectedFeaturesAndSources().test {
        expectItem() shouldContainExactly listOf(
            First.C to null,
            Second.B to null,
            Sourced.A to Sourced.Source.Local
        )

        viewModel.selectFeature(Sourced.Source.Remote)

        expectItem() shouldContainExactly listOf(
            First.C to null,
            Second.B to null,
            Sourced.A to Sourced.Source.Remote
        )
      }
    }

    it("resets feature flags to default options declared in factory") {
      val defaultOptionFactory = object : DefaultOptionFactory {
        override fun <T : Feature<T>> create(feature: T): Feature<*>? = when (feature) {
          is First -> First.A
          is Second -> Second.A
          else -> null
        }
      }
      val laboratory = Laboratory.builder()
          .featureStorage(FeatureStorage.inMemory())
          .defaultOptionFactory(defaultOptionFactory)
          .build()
      val viewModel = GroupViewModel(laboratory, NoSourceFeatureFactory)

      viewModel.observeSelectedFeatures().test {
        expectItem() shouldContainExactly listOf(First.A, Second.A)

        viewModel.selectFeature(First.B)
        expectItem() shouldContainExactly listOf(First.B, Second.A)

        viewModel.selectFeature(Second.B)
        expectItem() shouldContainExactly listOf(First.B, Second.B)

        laboratory.clear()
        expectItemEventually { it shouldContainExactly listOf(First.A, Second.A) }

        cancel()
      }
    }
  }
})

internal fun GroupViewModel.observeSelectedFeaturesAndSources() = observeFeatureGroups().map { groups ->
  groups.map { group ->
    val option = group.models.single(OptionUiModel::isSelected).option
    val source = group.sources.singleOrNull(OptionUiModel::isSelected)?.option
    option to source
  }
}

internal fun GroupViewModel.observeSelectedFeatures() = observeSelectedFeaturesAndSources().map { pairs ->
  pairs.map { (feature, _) -> feature }
}

private object NoSourceFeatureFactory : FeatureFactory {
  override fun create(): Set<Class<Feature<*>>> {
    @Suppress("UNCHECKED_CAST")
    return setOf(Second::class.java, First::class.java, Empty::class.java) as Set<Class<Feature<*>>>
  }
}

private object SourcedFeatureFactory : FeatureFactory {
  override fun create(): Set<Class<Feature<*>>> {
    @Suppress("UNCHECKED_CAST")
    return setOf(Sourced::class.java) as Set<Class<Feature<*>>>
  }
}

private object AllFeatureFactory : FeatureFactory {
  override fun create(): Set<Class<Feature<*>>> {
    return NoSourceFeatureFactory.create() + SourcedFeatureFactory.create()
  }
}

private enum class First : Feature<First> {
  C,
  B,
  A,
  ;

  override val defaultOption get() = C
}

private enum class Second : Feature<Second> {
  B,
  C,
  A,
  ;

  override val defaultOption get() = B
}

private enum class Empty : Feature<Empty>

private enum class Sourced : Feature<Sourced> {
  A,
  B,
  C,
  ;

  override val defaultOption get() = A

  @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  enum class Source : Feature<Source> {
    Local,
    Remote,
    ;

    override val defaultOption get() = Local
  }
}
