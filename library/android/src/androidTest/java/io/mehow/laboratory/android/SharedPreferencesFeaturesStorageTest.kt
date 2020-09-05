package io.mehow.laboratory.android

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Feature
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class SharedPreferencesFeaturesStorageTest {
  private val preferences = ApplicationProvider
    .getApplicationContext<Context>()
    .getSharedPreferences("laboratory", MODE_PRIVATE)
  private val storage = SharedPreferencesFeatureStorage(preferences)
  private val laboratory = Laboratory(storage)

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test fun storedFeatureIsAvailableAsExperiment() = runBlockingTest {
    storage.setFeature(FeatureA.B)

    laboratory.experiment<FeatureA>() shouldBe FeatureA.B
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test fun corruptedFeatureYieldsDefaultExperiment() = runBlockingTest {
    storage.setFeature(FeatureA.B)
    preferences.edit().putInt(FeatureA::class.java.name, 1).commit()

    laboratory.experiment<FeatureA>() shouldBe FeatureA.A
  }
}

private enum class FeatureA(override val isFallbackValue: Boolean = false) : Feature<FeatureA> {
  A,
  B,
  ;
}
