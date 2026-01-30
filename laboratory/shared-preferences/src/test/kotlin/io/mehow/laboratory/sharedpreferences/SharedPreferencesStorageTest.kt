package io.mehow.laboratory.sharedpreferences

import android.content.Context
import io.mehow.laboratory.Storage
import io.mehow.laboratory.testing.AbstractStorageTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class SharedPreferencesStorageTest : AbstractStorageTest() {
  override fun storage(): Storage {
    val sharedPreferences =
      RuntimeEnvironment.getApplication().getSharedPreferences("laboratory", Context.MODE_PRIVATE)
    return Storage.sharedPreferences(sharedPreferences)
  }
}
