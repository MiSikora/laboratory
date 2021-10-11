package io.mehow.laboratory.sample.multimodule

import android.os.Bundle
import android.widget.TextView
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.sample.multimodule.Application.Companion.laboratory
import io.mehow.laboratory.sample.multimodule.databinding.MainBinding
import io.mehow.laboratory.smaple.multimodule.a.Authentication
import io.mehow.laboratory.smaple.multimodule.b.LogType
import io.mehow.laboratory.smaple.multimodule.b.ShowAds
import io.mehow.laboratory.smaple.multimodule.c.Camera
import io.mehow.laboratory.smaple.multimodule.c.LivestreamPreview
import io.mehow.laboratory.smaple.multimodule.c.MotionDetection
import io.mehow.laboratory.smaple.multimodule.c.NightMode
import io.mehow.laboratory.smaple.multimodule.c.RecordingDirectory
import io.mehow.laboratory.smaple.multimodule.c.RecordingQuality
import io.mehow.laboratory.smaple.multimodule.c.VideoFilter
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
      authentication.observeFeature<Authentication>()
      logType.observeFeature<LogType>()
      showAds.observeFeature<ShowAds>()
      camera.observeFeature<Camera>()
      livestreamPreview.observeFeature<LivestreamPreview>()
      recordingQuality.observeFeature<RecordingQuality>()
      recordingDirectory.observeFeature<RecordingDirectory>()
      videoFilter.observeFeature<VideoFilter>()
      motionDetection.observeFeature<MotionDetection>()
      nightMode.observeFeature<NightMode>()
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
