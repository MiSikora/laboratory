package io.mehow.laboratory.inspector

import app.cash.turbine.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestScope
import io.kotest.engine.coroutines.backgroundScope
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.test.LocalFeature
import io.mehow.laboratory.inspector.test.NoOpDeprecationHandler
import io.mehow.laboratory.inspector.test.RemoteFeature
import io.mehow.laboratory.testing.perTest
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class InspectionViewModelSpec : FunSpec() {
  init {
    coroutineTestScope = true

    val laboratory by perTest { Laboratory.inMemory() }
    val searchQueries by perTest { MutableStateFlow(QueryString.Empty) }
    val loaders by perTest { mapOf("test" to TestLoader) }

    fun TestScope.createViewModel() =
      InspectionViewModel(
        laboratory,
        searchQueries,
        loaders,
        backgroundScope,
        EmptyCoroutineContext,
      )

    test("search interaction") {
      val viewModel = createViewModel()

      viewModel.sectionFlow("test").test {
        awaitItem().shouldBeEmpty()

        var features = awaitItem().map(FeatureMetadata::name)
        features shouldContainExactly listOf("LocalFeature", "RemoteFeature")

        searchQueries.value = QueryString.create("local")
        features = awaitItem().map(FeatureMetadata::name)
        features shouldContainExactly listOf("LocalFeature")

        searchQueries.value = QueryString.create("feature")
        features = awaitItem().map(FeatureMetadata::name)
        features shouldContainExactly listOf("LocalFeature", "RemoteFeature")

        searchQueries.value = QueryString.create("rem feat")
        features = awaitItem().map(FeatureMetadata::name)
        features shouldContainExactly listOf("RemoteFeature")

        searchQueries.value = QueryString.create("value1")
        features = awaitItem().map(FeatureMetadata::name)
        features shouldContainExactly listOf("LocalFeature")

        searchQueries.value = QueryString.create("unknown")
        awaitItem().shouldBeEmpty()
      }
    }
  }
}

private val TestLoader =
  FeatureMetadata.Loader(
    object : FeatureFactory {
      override fun create() =
        setOf(LocalFeature::class.java, RemoteFeature::class.java) as Set<Class<out Feature<*>>>
    },
    NoOpDeprecationHandler,
  )
