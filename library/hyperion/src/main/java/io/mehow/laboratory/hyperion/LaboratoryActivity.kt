package io.mehow.laboratory.hyperion

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
import io.mehow.laboratory.SubjectFactory
import io.mehow.laboratory.SubjectStorage

class LaboratoryActivity : Activity() {
  private lateinit var presenter: Presenter

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    presenter = lastNonConfigurationInstance as Presenter
    setContentView(R.layout.io_mehow_laboratory)
    val container = findViewById<ViewGroup>(R.id.io_mehow_laboratory_container)
    val spacing = resources.getDimensionPixelSize(R.dimen.io_mehow_laboratory_spacing)
    for (group in presenter.getSubjectGroups()) {
      val groupLabel = createSubjectGroupLabel(group, spacing)
      val groupView = SubjectGroupView(this, group, presenter::selectSubject)
      container.addView(groupLabel)
      container.addView(groupView)
    }
  }

  override fun getLastNonConfigurationInstance(): Any? {
    return super.getLastNonConfigurationInstance() ?: requireNotNull(presenterFactory) {
      "LaboratoryActivity must be initialized before using it."
    }.invoke()
  }

  override fun onRetainNonConfigurationInstance(): Any? = presenter

  private fun createSubjectGroupLabel(group: SubjectGroup, spacing: Int): TextView {
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

    fun initialize(factory: SubjectFactory, storage: SubjectStorage) {
      presenterFactory = { Presenter(factory, storage) }
    }

    fun start(context: Context) {
      context.startActivity(Intent(context, LaboratoryActivity::class.java))
    }
  }
}
