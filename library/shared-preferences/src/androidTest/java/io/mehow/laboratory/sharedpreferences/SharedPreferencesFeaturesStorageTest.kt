package io.mehow.laboratory.sharedpreferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mehow.laboratory.Feature
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class SharedPreferencesFeaturesStorageTest {
  private val preferences = ApplicationProvider
      .getApplicationContext<Context>()
      .getSharedPreferences("laboratory", MODE_PRIVATE)
  private val storage = FeatureStorage.sharedPreferences(preferences)
  private val laboratory = Laboratory.create(storage)

  @Test fun storedOptionIsAvailableAsExperiment() = runBlocking {
    storage.setOption(FeatureA.B)

    laboratory.experiment<FeatureA>() shouldBe FeatureA.B
  }

  @Test fun corruptedFeatureFlagOptionYieldsDefaultExperiment() = runBlocking {
    storage.setOption(FeatureA.B)
    preferences.edit().putInt(FeatureA::class.java.name, 1).commit()

    laboratory.experiment<FeatureA>() shouldBe FeatureA.A
  }

  @Test fun observesFeatureFlagChanges() = runBlocking {
    storage.observeFeatureName(FeatureA::class.java).test {
      expectItem() shouldBe null

      storage.setOption(FeatureA.B)
      expectItem() shouldBe FeatureA.B.name

      storage.setOption(FeatureA.B)
      expectNoEvents()

      storage.setOption(FeatureA.A)
      expectItem() shouldBe FeatureA.A.name
    }
  }

  @Test fun clearsFeatureFlagOptions() = runBlocking {
    storage.setOption(FeatureA.B)
    storage.clear()

    laboratory.experimentIs(FeatureA.A).shouldBeTrue()
  }
}

private enum class FeatureA : Feature<FeatureA> {
  A,
  B,
  ;

  override val defaultOption get() = A
}
