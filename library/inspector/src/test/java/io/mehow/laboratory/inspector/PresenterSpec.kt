package io.mehow.laboratory.inspector

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory

class PresenterSpec : DescribeSpec({
  describe("presenter") {
    it("filters empty feature groups") {
      val presenter = Presenter(AllFeatureFactory, Laboratory.inMemory())

      val featureNames = presenter.getFeatureGroups().map(FeatureGroup::name)

      featureNames shouldNotContain "Empty"
    }

    it("orders feature groups by name") {
      val presenter = Presenter(AllFeatureFactory, Laboratory.inMemory())

      val featureNames = presenter.getFeatureGroups().map(FeatureGroup::name)

      featureNames shouldContainExactly listOf("First", "Second")
    }

    it("does not order feature values") {
      val presenter = Presenter(AllFeatureFactory, Laboratory.inMemory())

      val features = presenter.getFeatureGroups()
        .map(FeatureGroup::models)
        .map { models -> models.map(FeatureModel::feature) }

      features[0] shouldContainExactly listOf(First.C, First.B, First.A)
      features[1] shouldContainExactly listOf(Second.B, Second.C, Second.A)
    }

    it("marks first feature as selected by default") {
      val presenter = Presenter(AllFeatureFactory, Laboratory.inMemory())

      presenter.getSelectedFeatures() shouldContainExactly listOf(First.C, Second.B)
    }

    it("marks saved feature as selected") {
      val storage = Laboratory.inMemory().apply {
        setFeature(First.A)
        setFeature(Second.C)
      }
      val presenter = Presenter(AllFeatureFactory, storage)

      presenter.getSelectedFeatures() shouldContainExactly listOf(First.A, Second.C)
    }

    it("selects features") {
      val presenter = Presenter(AllFeatureFactory, Laboratory.inMemory())

      presenter.selectFeature(First.B)
      presenter.selectFeature(Second.A)

      presenter.getSelectedFeatures() shouldContainExactly listOf(First.B, Second.A)
    }
  }
})

internal suspend fun Presenter.getSelectedFeatures(): List<Feature<*>> {
  return getFeatureGroups()
    .map(FeatureGroup::models)
    .map { models -> models.single(FeatureModel::isSelected).let(FeatureModel::feature) }
}

private object AllFeatureFactory : FeatureFactory {
  override fun create(): Set<Class<Feature<*>>> {
    @Suppress("UNCHECKED_CAST")
    return setOf(Second::class.java, First::class.java, Empty::class.java) as Set<Class<Feature<*>>>
  }
}

private enum class First(override val isFallbackValue: Boolean = false) : Feature<First> {
  C,
  B,
  A,
  ;
}

private enum class Second(override val isFallbackValue: Boolean = false) : Feature<Second> {
  B,
  C,
  A,
  ;
}

private enum class Empty
