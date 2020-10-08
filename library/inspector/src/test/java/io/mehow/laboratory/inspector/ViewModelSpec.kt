package io.mehow.laboratory.inspector

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration

class ViewModelSpec : DescribeSpec({
  describe("view model") {
    it("filters empty feature groups") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      val featureNames = viewModel.getFeatureGroups("Local").map(FeatureGroup::name)

      featureNames shouldNotContain "Empty"
    }

    it("orders feature groups by name") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      val featureNames = viewModel.getFeatureGroups("Local").map(FeatureGroup::name)

      featureNames shouldContainExactly listOf("First", "Second")
    }

    it("does not order feature values") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      val features = viewModel.getFeatureGroups("Local")
        .map(FeatureGroup::models)
        .map { models -> models.map(FeatureModel::feature) }

      features[0] shouldContainExactly listOf(First.C, First.B, First.A)
      features[1] shouldContainExactly listOf(Second.B, Second.C, Second.A)
    }

    it("marks first feature as selected by default") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      viewModel.getSelectedFeatures("Local") shouldContainExactly listOf(First.C, Second.B)
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

      viewModel.getSelectedFeatures("Local") shouldContainExactly listOf(First.A, Second.C)
    }

    it("selects features") {
      val viewModel = FeaturesViewModel(
        Configuration(FeatureStorage.inMemory(), mapOf("Local" to AllFeatureFactory))
      )

      viewModel.selectFeature(First.B)
      viewModel.selectFeature(Second.A)

      viewModel.getSelectedFeatures("Local") shouldContainExactly listOf(First.B, Second.A)
    }
  }
})

internal suspend fun FeaturesViewModel.getSelectedFeatures(groupName: String): List<Feature<*>> {
  return getFeatureGroups(groupName)
    .map(FeatureGroup::models)
    .map { models -> models.single(FeatureModel::isSelected).let(FeatureModel::feature) }
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
