package io.mehow.laboratory.sharedpreferences

import android.content.Context.MODE_PRIVATE
import androidx.test.platform.app.InstrumentationRegistry
import br.com.colman.kotest.FunSpec
import io.mehow.laboratory.Storage
import io.mehow.laboratory.testing.addStorageSpec

class SharedPreferencesStorageTest : FunSpec() {
  init {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val sharedPrefs = context.getSharedPreferences("laboratory", MODE_PRIVATE)
    val storage = Storage.sharedPreferences(sharedPrefs)

    addStorageSpec(storage)
  }
}
