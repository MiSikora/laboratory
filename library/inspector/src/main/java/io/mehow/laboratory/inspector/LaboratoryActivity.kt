package io.mehow.laboratory.inspector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.google.android.material.textview.MaterialTextView
import com.willowtreeapps.hyperion.plugin.v1.HyperionIgnore
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@HyperionIgnore // https://github.com/willowtreeapps/Hyperion-Android/issues/194
class LaboratoryActivity : Activity() {
  private lateinit var presenter: Presenter
  private val mainScope = MainScope()

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    presenter = lastNonConfigurationInstance as Presenter
    setContentView(R.layout.io_mehow_laboratory)
    val container = findViewById<ViewGroup>(R.id.io_mehow_laboratory_container)
    val spacing = resources.getDimensionPixelSize(R.dimen.io_mehow_laboratory_spacing)
    mainScope.launch {
      for (group in presenter.getFeatureGroups()) {
        val groupLabel = createFeatureGroupLabel(group, spacing)
        val groupView = FeatureGroupView(this@LaboratoryActivity, group) { feature ->
          mainScope.launch { presenter.selectFeature(feature) }
        }
        container.addView(groupLabel)
        container.addView(groupView)
      }
    }
  }

  override fun onDestroy() {
    mainScope.cancel()
    super.onDestroy()
  }

  override fun getLastNonConfigurationInstance(): Any? {
    return super.getLastNonConfigurationInstance() ?: requireNotNull(presenterFactory) {
      "LaboratoryActivity must be initialized before using it."
    }.invoke()
  }

  override fun onRetainNonConfigurationInstance(): Any? = presenter

  private fun createFeatureGroupLabel(group: FeatureGroup, spacing: Int): TextView {
    return MaterialTextView(this).apply {
      layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
        setMargins(spacing, spacing, spacing, 0)
      }
      TextViewCompat.setTextAppearance(this, R.style.TextAppearance_MaterialComponents_Headline5)
      text = group.name
    }
  }

  companion object {
    internal var presenterFactory: (() -> Presenter)? = null

    fun initialize(factory: FeatureFactory, storage: FeatureStorage) {
      presenterFactory = { Presenter(factory, storage) }
    }

    fun start(context: Context) {
      context.startActivity(Intent(context, LaboratoryActivity::class.java))
    }
  }
}
