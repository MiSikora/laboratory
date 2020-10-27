package io.mehow.laboratory.inspector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.OVER_SCROLL_NEVER
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.willowtreeapps.hyperion.plugin.v1.HyperionIgnore
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.launch

/**
 * Entry point for QA module that allows to interact with feature flags.
 */
@HyperionIgnore // https://github.com/willowtreeapps/Hyperion-Android/issues/194
public class LaboratoryActivity : AppCompatActivity() {
  private val viewModel by viewModels<FeaturesViewModel> {
    FeaturesViewModel.Factory(configuration)
  }

  private lateinit var resetFeaturesDialog: AlertDialog

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    setContentView(R.layout.io_mehow_laboratory_features)
    resetFeaturesDialog = createResetFeaturesDialog()
    setUpViewPager()
    setUpToolbar()
  }

  private fun createResetFeaturesDialog() = MaterialAlertDialogBuilder(this)
      .setTitle(R.string.io_mehow_laboratory_reset_title)
      .setMessage(R.string.io_mehow_laboratory_reset_message)
      .setNegativeButton(R.string.io_mehow_laboratory_cancel) { _, _ -> }
      .setPositiveButton(R.string.io_mehow_laboratory_reset) { _, _ -> resetFeatures() }
      .create()

  private fun setUpViewPager() {
    val sections = configuration.featureFactories.keys.toList()
    val viewPager = findViewById<ViewPager2>(R.id.io_mehow_laboratory_view_pager).apply {
      adapter = FeaturesAdapter(this@LaboratoryActivity, sections)
      disableScrollEffect()
    }
    if (sections.size <= 1) return
    val tabLayout = findViewById<TabLayout>(R.id.io_mehow_laboratory_tab_layout)
    tabLayout.isVisible = true
    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
      tab.text = sections[position]
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

  // TODO: Set this from XML. https://issuetracker.google.com/issues/134912610
  private fun ViewPager2.disableScrollEffect() {
    (getChildAt(0) as? RecyclerView)?.overScrollMode = OVER_SCROLL_NEVER
  }

  /**
   * Initialisation data for QA module.
   *
   * @param laboratory [Laboratory] instance that should share [FeatureStorage][io.mehow.laboratory.FeatureStorage]
   * instance with your application.
   * @param featureFactories Each entry in this map will result in a separate tab in the QA module. Key is used
   * as a tab name, and each tab displays all feature flags provided by [FeatureFactory] from value.
   */
  public class Configuration(
    internal val laboratory: Laboratory,
    internal val featureFactories: Map<String, FeatureFactory>,
  )

  public companion object {
    private const val featuresLabel = "Features"
    private const val sourcesLabel = "Sources"

    internal var backingConfiguration: Configuration? = null
      private set
    internal val configuration
      get() = requireNotNull(backingConfiguration) {
        "LaboratoryActivity must be initialized before using it."
      }

    /**
     * Configures [LaboratoryActivity] with a default "Features" tab, where feature flags are taken from the
     * [featureFactory]. Any additional tabs can be added in [externalFeatureFactories].
     */
    public fun configure(
      laboratory: Laboratory,
      featureFactory: FeatureFactory,
      externalFeatureFactories: Map<String, FeatureFactory> = emptyMap(),
    ) {
      val filteredFactories = externalFeatureFactories.filter { it.key == featuresLabel }
      configure(Configuration(
          laboratory,
          linkedMapOf(featuresLabel to featureFactory) + filteredFactories
      ))
    }

    /**
     * Configures [LaboratoryActivity] with a default "Features" and "Sources" tabs, where feature flags are taken
     * from the [featureFactory] and [featureSourceFactory] respectively.
     * Any additional tabs can be added in [externalFeatureFactories].
     */
    public fun configure(
      laboratory: Laboratory,
      featureFactory: FeatureFactory,
      featureSourceFactory: FeatureFactory,
      externalFeatureFactories: Map<String, FeatureFactory> = emptyMap(),
    ) {
      val filteredFactories = externalFeatureFactories.filter { it.key in listOf(featuresLabel, sourcesLabel) }
      configure(Configuration(
          laboratory,
          linkedMapOf(featuresLabel to featureFactory, sourcesLabel to featureSourceFactory) + filteredFactories
      ))
    }

    /**
     * Configures [LaboratoryActivity] with an input [configuration].
     */
    public fun configure(configuration: Configuration) {
      backingConfiguration = configuration
    }

    /**
     * Opens QA module. [Configure][configure] needs to be called before you interact with [LaboratoryActivity].
     */
    public fun start(context: Context) {
      context.startActivity(Intent(context, LaboratoryActivity::class.java))
    }
  }
}
