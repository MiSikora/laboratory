package io.mehow.laboratory.sharedpreferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
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

  @Test fun storedFeatureIsAvailableAsExperiment() = runBlocking {
    storage.setOption(FeatureA.B)

    laboratory.experiment<FeatureA>() shouldBe FeatureA.B
  }

  @Test fun corruptedFeatureYieldsDefaultExperiment() = runBlocking {
    storage.setOption(FeatureA.B)
    preferences.edit().putInt(FeatureA::class.java.name, 1).commit()

    laboratory.experiment<FeatureA>() shouldBe FeatureA.A
  }

  @Test fun observesFeatureChanges() = runBlocking {
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
}

private enum class FeatureA : Feature<FeatureA> {
  A,
  B,
  ;

  override val defaultOption get() = A
}
