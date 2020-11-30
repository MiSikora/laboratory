package io.mehow.laboratory.inspector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.OVER_SCROLL_NEVER
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.willowtreeapps.hyperion.plugin.v1.HyperionIgnore
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Entry point for QA module that allows to interact with feature flags.
 */
@HyperionIgnore // https://github.com/willowtreeapps/Hyperion-Android/issues/194
public class LaboratoryActivity : AppCompatActivity(R.layout.io_mehow_laboratory_inspector) {
  private val searchViewModel by viewModels<SearchViewModel> { SearchViewModel.Factory }

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    setUpToolbar()
    setUpViewPager()
  }

  private fun setUpToolbar() {
    val binding = ToolbarBinding(
        view = window.decorView,
        onSearchEventsListener = { event -> searchViewModel.sendEvent(event) },
        onResetEventsListener = { resetFeatureFlags() },
    )
    searchViewModel.uiModels
        .onEach { uiModel -> binding.render(uiModel) }
        .launchIn(lifecycleScope)
  }

  private fun setUpViewPager() {
    val sectionNames = configuration.sectionNames.toList()
    val viewPager = findViewById<ViewPager2>(R.id.io_mehow_laboratory_view_pager).apply {
      adapter = GroupAdapter(this@LaboratoryActivity, sectionNames)
      disableScrollEffect()
    }
    if (sectionNames.size <= 1) return
    val tabLayout = findViewById<TabLayout>(R.id.io_mehow_laboratory_tab_layout).apply {
      isVisible = true
    }
    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
      tab.text = sectionNames[position]
    }.attach()
  }

  private fun resetFeatureFlags() = lifecycleScope.launch {
    val isCleared = configuration.laboratory.clear()
    val messageId = if (isCleared) {
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
   * Configuration data for QA module.
   */
  public class Configuration internal constructor(
    builder: Builder,
  ) {
    internal val laboratory = builder.laboratory
    private val featureFactories = builder.featureFactories
    internal val deprecation = DeprecationHandler(builder.phenotypeSelector, builder.alignmentSelector)

    @Deprecated(
        message = "This method will be removed in 1.0.0. Use 'Configuration.create()' instead.",
        replaceWith = ReplaceWith("Configuration.create(laboratory, featureFactories)")
    )
    public constructor(
      laboratory: Laboratory,
      featureFactories: Map<String, FeatureFactory>,
    ) : this(Builder().apply {
      this.laboratory = laboratory
      this.featureFactories = featureFactories
    })

    internal val sectionNames = featureFactories.keys

    internal fun factory(sectionName: String) = featureFactories.getValue(sectionName)

    internal class Builder : LaboratoryStep, FeatureFactoriesStep, BuildingStep {
      lateinit var laboratory: Laboratory

      override fun laboratory(laboratory: Laboratory): FeatureFactoriesStep = apply {
        this.laboratory = laboratory
      }

      lateinit var featureFactories: Map<String, FeatureFactory>

      override fun featureFactories(factories: Map<String, FeatureFactory>): BuildingStep = apply {
        this.featureFactories = factories
      }

      internal var phenotypeSelector = DeprecationPhenotype.Selector { DeprecationPhenotype.Strikethrough }

      override fun deprecationPhenotypeSelector(selector: DeprecationPhenotype.Selector): BuildingStep = apply {
        this.phenotypeSelector = selector
      }

      internal var alignmentSelector = DeprecationAlignment.Selector { DeprecationAlignment.Bottom }

      override fun deprecationAlignmentSelector(selector: DeprecationAlignment.Selector): BuildingStep = apply {
        this.alignmentSelector = selector
      }

      override fun build(): Configuration = Configuration(this)
    }

    public companion object {
      /**
       * Creates [Configuration] with provided [laboratory] and [featureFactories].
       */
      public fun create(
        laboratory: Laboratory,
        featureFactories: Map<String, FeatureFactory>,
      ): Configuration = builder().laboratory(laboratory).featureFactories(featureFactories).build()

      /**
       * Creates a builder that allows to customize [Configuration].
       */
      public fun builder(): LaboratoryStep = Builder()
    }

    /**
     * A step of a fluent builder that requires [Laboratory] to proceed.
     */
    public interface LaboratoryStep {
      /**
       * Sets laboratory. Its instance should share [FeatureStorage][io.mehow.laboratory.FeatureStorage]
       * instance with your application.
       */
      public fun laboratory(laboratory: Laboratory): FeatureFactoriesStep
    }

    /**
     * A step of a fluent builder that requires [feature factories][FeatureFactory] to proceed.
     */
    public interface FeatureFactoriesStep {
      /**
       * Sets feature factories. Each entry in this map will result in a separate tab in the QA module. Key is used
       * as a tab name, and each tab displays all feature flags provided by [FeatureFactory] from value.
       */
      public fun featureFactories(factories: Map<String, FeatureFactory>): BuildingStep
    }

    /**
     * The final step of a fluent builder that can set optional parameters.
     */
    public interface BuildingStep {
      /**
       * Sets how deprecated feature flags will be displayed to the user.
       */
      public fun deprecationPhenotypeSelector(selector: DeprecationPhenotype.Selector): BuildingStep

      /**
       * Sets how deprecated feature flags will be sorted in a displayed group.
       */
      public fun deprecationAlignmentSelector(selector: DeprecationAlignment.Selector): BuildingStep

      /**
       * Creates a new [Configuration] with provided parameters.
       */
      public fun build(): Configuration
    }
  }

  public companion object {
    private const val featuresLabel = "Features"
    internal lateinit var configuration: Configuration
      private set

    /**
     * Configures [LaboratoryActivity] with a default "Features" tab, where feature flags are taken from the
     * [mainFactory]. Any additional tabs can be added in [externalFactories].
     */
    public fun configure(
      laboratory: Laboratory,
      mainFactory: FeatureFactory,
      externalFactories: Map<String, FeatureFactory> = emptyMap(),
    ) {
      val filteredFactories = externalFactories.filterNot { it.key == featuresLabel }
      configure(Configuration.create(
          laboratory,
          featureFactories = linkedMapOf(featuresLabel to mainFactory) + filteredFactories
      ))
    }

    /**
     * Configures [LaboratoryActivity] with an input [configuration].
     */
    public fun configure(configuration: Configuration) {
      this.configuration = configuration
    }

    /**
     * Opens QA module. [Configure][configure] needs to be called before you interact with [LaboratoryActivity].
     */
    public fun start(context: Context) {
      check(::configuration.isInitialized) {
        "${LaboratoryActivity::class.java} must be initialized before using it."
      }
      context.startActivity(Intent(context, LaboratoryActivity::class.java))
    }
  }
}
