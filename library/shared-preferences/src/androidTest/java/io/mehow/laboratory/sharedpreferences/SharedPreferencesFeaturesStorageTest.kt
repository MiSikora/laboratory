package io.mehow.laboratory.sharedpreferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Feature
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.time.ExperimentalTime

class SharedPreferencesFeaturesStorageTest {
  private val preferences = ApplicationProvider
    .getApplicationContext<Context>()
    .getSharedPreferences("laboratory", MODE_PRIVATE)
  private val storage = SharedPreferencesFeatureStorage(preferences)
  private val laboratory = Laboratory(storage)

  @Test fun storedFeatureIsAvailableAsExperiment() = runBlocking {
    storage.setFeature(FeatureA.B)

    laboratory.experiment<FeatureA>() shouldBe FeatureA.B
  }

  @Test fun corruptedFeatureYieldsDefaultExperiment() = runBlocking {
    storage.setFeature(FeatureA.B)
    preferences.edit().putInt(FeatureA::class.java.name, 1).commit()

    laboratory.experiment<FeatureA>() shouldBe FeatureA.A
  }

  @Test fun observesFeatureChanges() = runBlocking {
    @OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
    storage.observeFeatureName(FeatureA::class.java).test {
      expectItem() shouldBe null

      storage.setFeature(FeatureA.B)
      expectItem() shouldBe FeatureA.B.name

      storage.setFeature(FeatureA.B)
      expectNoEvents()

      storage.setFeature(FeatureA.A)
      expectItem() shouldBe FeatureA.A.name
    }
  }
}

private enum class FeatureA(override val isDefaultValue: Boolean = false) : Feature<FeatureA> {
  A,
  B,
  ;
}
