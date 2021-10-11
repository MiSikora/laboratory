package io.mehow.laboratory.sample.defaultoption

import android.os.Bundle
import android.widget.TextView
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.sample.defaultoption.Application.Companion.laboratory
import io.mehow.laboratory.sample.defaultoption.databinding.MainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import android.app.Activity as AndroidActivity

class Activity : AndroidActivity() {
  private val mainScope = MainScope()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = MainBinding.inflate(layoutInflater).apply {
      launchLaboratory.setOnClickListener { LaboratoryActivity.start(this@Activity) }
      showAds.observeFeature<ShowAds>()
      reportRootedDevice.observeFeature<ReportRootedDevice>()
      requiredFingerprint.observeFeature<RequiredFingerprint>()
    }
    setContentView(binding.root)
  }

  override fun onDestroy() {
    mainScope.cancel()
    super.onDestroy()
  }

  private inline fun <reified T : Feature<T>> TextView.observeFeature() {
    laboratory.observe<T>()
        .map { "${it.javaClass.simpleName}: $it" }
        .onEach { text = it }
        .launchIn(mainScope)
  }
}
