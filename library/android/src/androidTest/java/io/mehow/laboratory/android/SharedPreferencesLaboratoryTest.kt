package io.mehow.laboratory.android

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class SharedPreferencesLaboratoryTest {
  private val preferences = ApplicationProvider
    .getApplicationContext<Context>()
    .getSharedPreferences("laboratory", MODE_PRIVATE)
  private val storage = SharedPreferencesFeatureStorage(preferences)
  private val laboratory = Laboratory(storage)

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test fun storedFeatureIsAvailableAsExperiment() = runBlockingTest {
    storage.setFeature(Feature.B)

    laboratory.experiment<Feature>() shouldBe Feature.B
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test fun corruptedFeatureYieldsDefaultExperiment() = runBlockingTest {
    storage.setFeature(Feature.B)
    preferences.edit().putInt(Feature::class.java.name, 1).commit()

    laboratory.experiment<Feature>() shouldBe Feature.A
  }
}

private enum class Feature {
  A,
  B
}
