package io.mehow.laboratory.inspector

import app.cash.turbine.test
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class ViewModelSpec : DescribeSpec({
  describe("view model") {
    it("filters empty feature groups") {
      val viewModel = FeaturesViewModel(
          Configuration(Laboratory.inMemory(), mapOf("Local" to NoSourceFeatureFactory))
      )

      val featureNames = viewModel.observeFeatureGroups("Local").first().map(FeatureGroup::name)

      featureNames shouldNotContain "Empty"
    }

    it("orders feature groups by name") {
      val viewModel = FeaturesViewModel(
          Configuration(Laboratory.inMemory(), mapOf("Local" to NoSourceFeatureFactory))
      )

      val featureNames = viewModel.observeFeatureGroups("Local").first().map(FeatureGroup::name)

      featureNames shouldContainExactly listOf("First", "Second")
    }

    it("does not order feature values") {
      val viewModel = FeaturesViewModel(
          Configuration(Laboratory.inMemory(), mapOf("Local" to NoSourceFeatureFactory))
      )

      val features = viewModel.observeFeatureGroups("Local").first()
          .map(FeatureGroup::models)
          .map { models -> models.map(FeatureModel::feature) }

      features[0] shouldContainExactly listOf(First.C, First.B, First.A)
      features[1] shouldContainExactly listOf(Second.B, Second.C, Second.A)
    }

    it("marks first feature as selected by default") {
      val viewModel = FeaturesViewModel(
          Configuration(Laboratory.inMemory(), mapOf("Local" to NoSourceFeatureFactory))
      )

      viewModel.observeSelectedFeatures("Local").first() shouldContainExactly listOf(First.C, Second.B)
    }

    it("marks saved feature as selected") {
      val laboratory = Laboratory.inMemory().apply {
        setFeature(First.A)
        setFeature(Second.C)
      }

      val viewModel = FeaturesViewModel(
          Configuration(laboratory, mapOf("Local" to NoSourceFeatureFactory))
      )

      viewModel.observeSelectedFeatures("Local").first() shouldContainExactly listOf(First.A, Second.C)
    }

    it("selects features") {
      val viewModel = FeaturesViewModel(
          Configuration(Laboratory.inMemory(), mapOf("Local" to NoSourceFeatureFactory))
      )

      viewModel.selectFeature(First.B)
      viewModel.selectFeature(Second.A)

      viewModel.observeSelectedFeatures("Local").first() shouldContainExactly listOf(First.B, Second.A)
    }

    it("observes feature changes") {
      val viewModel = FeaturesViewModel(
          Configuration(Laboratory.inMemory(), mapOf("Local" to NoSourceFeatureFactory))
      )

      viewModel.observeSelectedFeatures("Local").test {
        expectItem() shouldContainExactly listOf(First.C, Second.B)

        viewModel.selectFeature(First.B)
        expectItem() shouldContainExactly listOf(First.B, Second.B)

        viewModel.selectFeature(Second.C)
        expectItem() shouldContainExactly listOf(First.B, Second.C)

        cancel()
      }
    }

    it("resets all features to their default values") {
      val viewModel = FeaturesViewModel(
          Configuration(
              Laboratory.inMemory(),
              mapOf(
                  "First" to object : FeatureFactory {
                    @Suppress("UNCHECKED_CAST")
                    override fun create() = setOf(Empty::class.java, First::class.java) as Set<Class<Feature<*>>>
                  },
                  "Second" to object : FeatureFactory {
                    @Suppress("UNCHECKED_CAST")
                    override fun create() = setOf(Second::class.java) as Set<Class<Feature<*>>>
                  },
              ),
          )
      )

      viewModel.selectFeature(First.B)
      viewModel.selectFeature(Second.C)

      viewModel.resetAllFeatures()

      viewModel.observeSelectedFeatures("First").first() shouldContainExactly listOf(First.C)
      viewModel.observeSelectedFeatures("Second").first() shouldContainExactly listOf(Second.B)
    }

    it("resets feature and sources to their default values") {
      val viewModel = FeaturesViewModel(
          Configuration(Laboratory.inMemory(), mapOf("Local" to SourcedFeatureFactory))
      )

      viewModel.selectFeature(Sourced.B)
      viewModel.selectFeature(Sourced.Source.Remote)

      viewModel.resetAllFeatures()

      viewModel.observeSelectedFeaturesAndSources("Local").first() shouldContainExactly listOf(
          Sourced.A to Sourced.Source.Local,
      )
    }

    it("observes source changes") {
      val viewModel = FeaturesViewModel(
          Configuration(Laboratory.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      viewModel.observeSelectedFeaturesAndSources("Local").test {
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
  }
})

internal fun FeaturesViewModel.observeSelectedFeaturesAndSources(
  section: String,
) = observeFeatureGroups(section).map { groups ->
  groups.map { group ->
    val feature = group.models.single(FeatureModel::isSelected).feature
    val source = group.sources.singleOrNull(FeatureModel::isSelected)?.feature
    feature to source
  }
}

internal fun FeaturesViewModel.observeSelectedFeatures(
  section: String,
) = observeSelectedFeaturesAndSources(section).map { pairs ->
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

private enum class First(override val isDefaultValue: Boolean = false) : Feature<First> {
  C,
  B,
  A,
  ;
}

private enum class Second(override val isDefaultValue: Boolean = false) : Feature<Second> {
  B,
  C,
  A,
  ;
}

private enum class Empty : Feature<Empty>

private enum class Sourced(override val isDefaultValue: Boolean = false) : Feature<Sourced> {
  A,
  B,
  C,
  ;

  @Suppress("UNCHECKED_CAST")
  override val source = Source::class.java as Class<Feature<*>>

  enum class Source(override val isDefaultValue: Boolean = false) : Feature<Source> {
    Local,
    Remote,
    ;
  }
}
