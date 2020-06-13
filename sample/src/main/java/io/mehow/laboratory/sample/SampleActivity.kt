package io.mehow.laboratory.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.brombulator.Brombulation
import io.mehow.laboratory.frombulator.Frombulation
import io.mehow.laboratory.inspector.LaboratoryActivity
import io.mehow.laboratory.trombulator.Trombulation

@SuppressLint("SetTextI18n")
class SampleActivity : Activity() {
  private lateinit var laboratory: Laboratory
  private lateinit var brombulation: TextView
  private lateinit var frombulation: TextView
  private lateinit var trombulation: TextView

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    laboratory = SampleApplication.getLaboratory(application)

    setContentView(R.layout.io_mehow_laboratory_sample_main)
    findViewById<Button>(R.id.launch_laboratory).setOnClickListener {
      LaboratoryActivity.start(this)
    }
    brombulation = findViewById(R.id.brombulation)
    frombulation = findViewById(R.id.frombulation)
    trombulation = findViewById(R.id.trombulation)
  }

  override fun onResume() {
    super.onResume()
    brombulation.text = "Brombulation: ${laboratory.experiment<Brombulation>()}"
    frombulation.text = "Frombulation: ${laboratory.experiment<Frombulation>()}"
    trombulation.text = "Trombulation: ${laboratory.experiment<Trombulation>()}"
  }
}
