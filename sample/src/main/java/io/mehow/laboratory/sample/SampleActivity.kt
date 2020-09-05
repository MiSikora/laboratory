package io.mehow.laboratory.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import io.mehow.laboratory.brombulator.Brombulation
import io.mehow.laboratory.frombulator.Frombulation
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.trombulator.Trombulation
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@SuppressLint("SetTextI18n")
class SampleActivity : Activity() {
  private val mainScope = MainScope()

  @Suppress("LongMethod", "StringLiteralDuplication")
  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    val laboratory = SampleApplication.getLaboratory(application)

    setContentView(R.layout.io_mehow_laboratory_sample_main)
    findViewById<Button>(R.id.launch_laboratory).setOnClickListener {
      LaboratoryActivity.start(this)
    }

    val brombulation = findViewById<TextView>(R.id.brombulation)
    val frombulation = findViewById<TextView>(R.id.frombulation)
    val trombulation = findViewById<TextView>(R.id.trombulation)

    laboratory.observe<Brombulation>()
      .onEach { brombulation.text = "${it.javaClass.simpleName}: $it" }
      .launchIn(mainScope)
    laboratory.observe<Frombulation>()
      .onEach { frombulation.text = "${it.javaClass.simpleName}: $it" }
      .launchIn(mainScope)
    laboratory.observe<Trombulation>()
      .onEach { trombulation.text = "${it.javaClass.simpleName}: $it" }
      .launchIn(mainScope)
  }

  override fun onDestroy() {
    mainScope.cancel()
    super.onDestroy()
  }
}
