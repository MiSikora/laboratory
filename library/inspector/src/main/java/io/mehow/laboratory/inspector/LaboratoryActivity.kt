package io.mehow.laboratory.inspector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.willowtreeapps.hyperion.plugin.v1.HyperionIgnore
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.FeatureStorage
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.launch

@HyperionIgnore // https://github.com/willowtreeapps/Hyperion-Android/issues/194
class LaboratoryActivity : AppCompatActivity() {
  private val viewModel by viewModels<FeaturesViewModel> {
    FeaturesViewModel.Factory(configuration)
  }

  private lateinit var resetFeaturesDialog: AlertDialog

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    setContentView(R.layout.io_mehow_laboratory)
    resetFeaturesDialog = MaterialAlertDialogBuilder(this)
      .setTitle(R.string.io_mehow_laboratory_reset_title)
      .setMessage(R.string.io_mehow_laboratory_reset_message)
      .setNegativeButton(R.string.io_mehow_laboratory_cancel) { _, _ -> }
      .setPositiveButton(R.string.io_mehow_laboratory_reset) { _, _ -> resetFeatures() }
      .create()
    setUpViewPager()
    setUpToolbar()
  }

  private fun setUpViewPager() {
    val groupNames = configuration.featureFactories.keys.toList()
    val viewPager = findViewById<ViewPager2>(R.id.io_mehow_laboratory_view_pager).apply {
      adapter = FeaturesAdapter(this@LaboratoryActivity, groupNames)
    }
    val tabLayout = findViewById<TabLayout>(R.id.io_mehow_laboratory_tab_layout)
    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
      tab.text = groupNames[position]
    }.attach()
  }

  private fun setUpToolbar() {
    val toolbar = findViewById<MaterialToolbar>(R.id.io_mehow_laboratory_toolbar)
    toolbar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.io_mehow_laboratory_reset_features_menu_item -> {
          showResetDialog()
          true
        }
        else -> false
      }
    }
  }

  private fun showResetDialog() {
    resetFeaturesDialog.show()
  }

  private fun resetFeatures() = lifecycleScope.launch {
    val didReset = viewModel.resetAllFeatures()
    val messageId = if (didReset) {
      R.string.io_mehow_laboratory_reset_success
    } else {
      R.string.io_mehow_laboratory_reset_failure
    }
    val root = findViewById<CoordinatorLayout>(R.id.io_mehow_laboratory_root)
    Snackbar.make(root, messageId, Snackbar.LENGTH_SHORT).show()
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
