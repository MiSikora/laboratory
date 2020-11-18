package io.mehow.laboratory.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import io.mehow.laboratory.Feature
import io.mehow.laboratory.a.AllowScreenshots
import io.mehow.laboratory.a.Authentication
import io.mehow.laboratory.b.PowerSource
import io.mehow.laboratory.c.DistanceAlgorithm
import io.mehow.laboratory.inspector.LaboratoryActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@SuppressLint("SetTextI18n")
class SampleActivity : Activity() {
  private val mainScope = MainScope()

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)

    setContentView(R.layout.io_mehow_laboratory_sample_main)
    findViewById<Button>(R.id.launch_laboratory).setOnClickListener {
      LaboratoryActivity.start(this)
    }

    findViewById<TextView>(R.id.authentication).observeFeature<Authentication>()
    findViewById<TextView>(R.id.powerSource).observeFeature<PowerSource>()
    findViewById<TextView>(R.id.distanceAlgorithm).observeFeature<DistanceAlgorithm>()
    findViewById<TextView>(R.id.logType).observeFeature<LogType>()
    findViewById<TextView>(R.id.reportRootedDevice).observeFeature<ReportRootedDevice>()
    findViewById<TextView>(R.id.showAds).observeFeature<ShowAds>()
    findViewById<TextView>(R.id.allowScreenshots).observeFeature<AllowScreenshots>()
  }

  private inline fun <reified T : Feature<T>> TextView.observeFeature() {
    val laboratory = SampleApplication.getLaboratory(application)
    laboratory.observe<T>()
        .onEach { this.text = "${it.javaClass.simpleName}: $it" }
        .launchIn(mainScope)
  }

  override fun onDestroy() {
    mainScope.cancel()
    super.onDestroy()
  }
}
