package io.mehow.laboratory.sample.supervision

import android.os.Bundle
import android.widget.TextView
import io.mehow.laboratory.Feature
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.sample.supervision.databinding.MainBinding
import io.mehow.laboratory.sample.supervision.Application.Companion.laboratory
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
      theming.observeFeature<Theming>()
      christmasGreeting.observeFeature<ChristmasGreeting>()
      christmasBackground.observeFeature<ChristmasBackground>()
      spookyMusic.observeFeature<SpookyMusic>()
      witchChance.observeFeature<WitchChance>()
      candyArt.observeFeature<CandyArt>()
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
