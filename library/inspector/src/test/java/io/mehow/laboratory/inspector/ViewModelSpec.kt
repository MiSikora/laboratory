package io.mehow.laboratory.inspector

import app.cash.turbine.test
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

class ViewModelSpec : DescribeSpec({
  describe("view model") {
    it("filters empty feature groups") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      val featureNames = viewModel.observeFeatureGroups("Local").first().map(FeatureGroup::name)

      featureNames shouldNotContain "Empty"
    }

    it("orders feature groups by name") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      val featureNames = viewModel.observeFeatureGroups("Local").first().map(FeatureGroup::name)

      featureNames shouldContainExactly listOf("First", "Second")
    }

    it("does not order feature values") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      val features = viewModel.observeFeatureGroups("Local").first()
        .map(FeatureGroup::models)
        .map { models -> models.map(FeatureModel::feature) }

      features[0] shouldContainExactly listOf(First.C, First.B, First.A)
      features[1] shouldContainExactly listOf(Second.B, Second.C, Second.A)
    }

    it("marks first feature as selected by default") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      viewModel.observeSelectedFeatures("Local").first() shouldContainExactly listOf(First.C, Second.B)
    }

    it("marks saved feature as selected") {
      val storage = FeatureStorage.inMemory()
      Laboratory(storage).apply {
        setFeature(First.A)
        setFeature(Second.C)
      }

      val viewModel = FeaturesViewModel(
        Configuration(storage, mapOf("Local" to AllFeatureFactory))
      )

      viewModel.observeSelectedFeatures("Local").first() shouldContainExactly listOf(First.A, Second.C)
    }

    it("selects features") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      viewModel.selectFeature(First.B)
      viewModel.selectFeature(Second.A)

      viewModel.observeSelectedFeatures("Local").first() shouldContainExactly listOf(First.B, Second.A)
    }

    it("observes feature changes") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      @OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
      viewModel.observeSelectedFeatures("Local").test {
        expectItem() shouldContainExactly listOf(First.C, Second.B)

        viewModel.selectFeature(First.B)
        expectItem() shouldContainExactly listOf(First.B, Second.B)

        viewModel.selectFeature(Second.C)
        expectItem() shouldContainExactly listOf(First.B, Second.C)

        cancel()
      }
    }
  }
})

internal fun FeaturesViewModel.observeSelectedFeatures(groupName: String): Flow<List<Feature<*>>> {
  return observeFeatureGroups(groupName).map { group ->
    group
      .map(FeatureGroup::models)
      .map { models -> models.single(FeatureModel::isSelected).feature }
  }
}

private object AllFeatureFactory : FeatureFactory {
  override fun create(): Set<Class<Feature<*>>> {
    @Suppress("UNCHECKED_CAST")
    return setOf(Second::class.java, First::class.java, Empty::class.java) as Set<Class<Feature<*>>>
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
