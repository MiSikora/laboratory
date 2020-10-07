package io.mehow.laboratory.inspector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.willowtreeapps.hyperion.plugin.v1.HyperionIgnore
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory

@HyperionIgnore // https://github.com/willowtreeapps/Hyperion-Android/issues/194
class LaboratoryActivity : AppCompatActivity() {
  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    setContentView(R.layout.io_mehow_laboratory)
    val groupNames = configuration.featureFactories.keys.toList()
    val viewPager = findViewById<ViewPager2>(R.id.io_mehow_laboratory_view_pager).apply {
      adapter = FeaturesAdapter(this@LaboratoryActivity, groupNames)
    }
    val tabLayout = findViewById<TabLayout>(R.id.io_mehow_laboratory_tab_layout)
    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
      tab.text = groupNames[position]
    }.attach()
  }

  class Configuration(
    localStorage: FeatureStorage,
    internal val featureFactories: Map<String, FeatureFactory>,
  ) {
    internal val laboratory = Laboratory(localStorage)
  }

  companion object {
    internal var backingConfiguration: Configuration? = null
      private set
    internal val configuration
      get() = requireNotNull(backingConfiguration) {
        "LaboratoryActivity must be initialized before using it."
      }

    fun configure(
      localStorage: FeatureStorage,
      featureFactory: FeatureFactory,
    ) {
      configure(Configuration(
        localStorage,
        linkedMapOf("Features" to featureFactory)
      ))
    }

    fun configure(
      localStorage: FeatureStorage,
      featureFactory: FeatureFactory,
      featureSourceFactory: FeatureFactory,
    ) {
      configure(Configuration(
        localStorage,
        linkedMapOf("Features" to featureFactory, "Sources" to featureSourceFactory)
      ))
    }

    fun configure(configuration: Configuration) {
      backingConfiguration = configuration
    }

    fun start(context: Context) {
      context.startActivity(Intent(context, LaboratoryActivity::class.java))
    }
  }
}
