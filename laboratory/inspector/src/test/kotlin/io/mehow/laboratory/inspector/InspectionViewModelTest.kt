package io.mehow.laboratory.inspector

import app.cash.turbine.test
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.test.LocalFeature
import io.mehow.laboratory.inspector.test.NoOpDeprecationHandler
import io.mehow.laboratory.inspector.test.RemoteFeature
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class InspectionViewModelTest {
  val scope = TestScope()

  private val searchQueries = MutableStateFlow(QueryString.Empty)

  private val viewModel =
    InspectionViewModel(
      laboratory = Laboratory.inMemory(),
      searchQueries = searchQueries,
      loaders =
        mapOf(
          "test" to
            FeatureMetadata.Loader(
              object : FeatureFactory {
                override fun create() =
                  setOf(LocalFeature::class.java, RemoteFeature::class.java)
                    as Set<Class<out Feature<*>>>
              },
              NoOpDeprecationHandler,
            )
        ),
      scope = scope.backgroundScope,
      computationDispatcher = EmptyCoroutineContext,
    )

  @Test
  fun `search interaction`() = scope.runTest {
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

      cancel()
    }
  }
}
