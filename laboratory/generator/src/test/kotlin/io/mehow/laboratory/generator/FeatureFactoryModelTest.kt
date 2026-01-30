package io.mehow.laboratory.generator

import com.squareup.kotlinpoet.ClassName
import io.mehow.laboratory.generator.Visibility.Internal
import io.mehow.laboratory.generator.Visibility.Public
import io.mehow.laboratory.generator.test.shouldSpecify
import org.junit.Test

class FeatureFactoryModelTest {
  val featureA =
    FeatureFlagModel(
      ClassName("io.mehow", "FeatureA"),
      listOf(FeatureFlagOption("A", isDefault = true)),
    )

  val featureB =
    FeatureFlagModel(
      ClassName("io.mehow", "FeatureB"),
      listOf(FeatureFlagOption("A", isDefault = true)),
    )

  val featureC =
    FeatureFlagModel(
      ClassName("io.mehow.c", "FeatureA"),
      listOf(FeatureFlagOption("A", isDefault = true)),
    )

  @Test
  fun `internal visibility`() {
    val model =
      FeatureFactoryModel(
        ClassName("io.mehow", "Factory"),
        listOf(featureA, featureB, featureC),
        visibility = Internal,
      )

    val fileSpec = model.prepare()

    fileSpec shouldSpecify
      """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.FeatureFactory
        import java.lang.Class
        import kotlin.Suppress
        import kotlin.collections.Set
        import kotlin.collections.setOf

        internal fun FeatureFactory.Companion.generated(): FeatureFactory = Factory

        private object Factory : FeatureFactory {
          @Suppress("UNCHECKED_CAST")
          override fun create(): Set<Class<out Feature<*>>> = setOf(
            Class.forName("io.mehow.FeatureA"),
            Class.forName("io.mehow.FeatureB"),
            Class.forName("io.mehow.c.FeatureA")
          ) as Set<Class<out Feature<*>>>
        }
        """
  }

  @Test
  fun `public visibility`() {
    val model =
      FeatureFactoryModel(
        ClassName("io.mehow", "Factory"),
        listOf(featureA, featureB, featureC),
        visibility = Public,
      )

    val fileSpec = model.prepare()

    fileSpec shouldSpecify
      """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.FeatureFactory
        import java.lang.Class
        import kotlin.Suppress
        import kotlin.collections.Set
        import kotlin.collections.setOf

        public fun FeatureFactory.Companion.generated(): FeatureFactory = Factory

        private object Factory : FeatureFactory {
          @Suppress("UNCHECKED_CAST")
          override fun create(): Set<Class<out Feature<*>>> = setOf(
            Class.forName("io.mehow.FeatureA"),
            Class.forName("io.mehow.FeatureB"),
            Class.forName("io.mehow.c.FeatureA")
          ) as Set<Class<out Feature<*>>>
        }
        """
  }

  @Test
  fun `no features`() {
    val model = FeatureFactoryModel(ClassName("io.mehow", "Factory"), features = emptyList())

    val fileSpec = model.prepare()

    fileSpec shouldSpecify
      """
        package io.mehow

        import io.mehow.laboratory.Feature
        import io.mehow.laboratory.FeatureFactory
        import java.lang.Class
        import kotlin.collections.Set
        import kotlin.collections.emptySet

        internal fun FeatureFactory.Companion.generated(): FeatureFactory = Factory

        private object Factory : FeatureFactory {
          override fun create(): Set<Class<out Feature<*>>> = emptySet<Class<out Feature<*>>>()
        }
        """
  }
}
