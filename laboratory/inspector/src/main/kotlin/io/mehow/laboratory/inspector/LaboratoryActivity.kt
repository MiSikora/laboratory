package io.mehow.laboratory.inspector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.willowtreeapps.hyperion.plugin.v1.HyperionIgnore
import io.mehow.laboratory.FeatureFactory
import io.mehow.laboratory.Laboratory
import io.mehow.laboratory.inspector.LaboratoryActivity.Companion.configure
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration.OffscreenSectionsBehavior.Limited
import io.mehow.laboratory.inspector.LaboratoryActivity.Configuration.OffscreenSectionsBehavior.Unlimited
import kotlinx.coroutines.launch

/**
 * Activity that serves as the entry point to the QA inspector module, providing a UI to browse and
 * modify feature flags at runtime.
 */
@HyperionIgnore // https://github.com/willowtreeapps/Hyperion-Android/issues/194
public class LaboratoryActivity : AppCompatActivity(R.layout.io_mehow_laboratory_inspector) {
  private val sectionNames = configuration.sectionNames.toList()

  internal val toolbarViewModel by
    viewModels<ToolbarViewModel> { ToolbarViewModel.Factory(configuration) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setUpToolbar()
    setUpViewPager()
  }

  private fun setUpToolbar() {
    val toolbar = findViewById<View>(R.id.io_mehow_laboratory_toolbar)
    val binding = ToolbarBinding(toolbar)
    lifecycleScope.launch {
      toolbarViewModel.uiModels.flowWithLifecycle(lifecycle).collect(binding::render)
    }
  }

  private fun setUpViewPager() {
    val viewPager =
      findViewById<ViewPager2>(R.id.io_mehow_laboratory_view_pager).apply {
        adapter = SectionAdapter(this@LaboratoryActivity, sectionNames)
        offscreenPageLimit = configuration.offscreenSectionCount
        disableScrollEffect()
        doOnApplyWindowInsets { view, insets, padding ->
          val insets = insets.getTopInsets()
          view.updatePadding(
            left = padding.left + insets.left,
            right = padding.right + insets.right,
          )
        }
      }

    if (sectionNames.size > 1) {
      val tabLayout =
        findViewById<TabLayout>(R.id.io_mehow_laboratory_tab_layout).apply { isVisible = true }
      TabLayoutMediator(tabLayout, viewPager, ::setTabName).attach()
    }
  }

  private fun setTabName(tab: TabLayout.Tab, position: Int) {
    tab.text = sectionNames[position]
  }

  /** Configuration settings for the QA module. */
  public class Configuration internal constructor(builder: Builder) {
    internal val laboratory = builder.laboratory

    internal val metadataLoaders = run {
      val deprecationHandler = DeprecationHandler(builder.styleSelector, builder.alignmentSelector)
      builder.featureFactories.mapValues { (_, featureFactory) ->
        FeatureMetadata.Loader(featureFactory, deprecationHandler)
      }
    }

    internal val sectionNames = metadataLoaders.keys

    internal val offscreenSectionCount =
      when (val behavior = builder.offscreenSectionsBehavior) {
        is Limited -> behavior.limit
        is Unlimited -> metadataLoaders.size
      }

    /**
     * Determines how the inspector handles feature sections (tabs) that are not currently visible.
     */
    public sealed class OffscreenSectionsBehavior {
      /**
       * Keeps all feature sections in memory, providing smoother navigation between sections at the
       * cost of a potentially slower initial load time.
       */
      public object Unlimited : OffscreenSectionsBehavior()

      /** Limits the number of offscreen sections retained in memory to the specified [limit]. */
      public class Limited(public val limit: Int) : OffscreenSectionsBehavior()
    }

    internal class Builder : LaboratoryStep, FeatureFactoriesStep, BuildingStep {
      lateinit var laboratory: Laboratory

      override fun laboratory(laboratory: Laboratory): FeatureFactoriesStep = apply {
        this.laboratory = laboratory
      }

      lateinit var featureFactories: Map<String, FeatureFactory>

      override fun featureFactories(factories: Map<String, FeatureFactory>): BuildingStep = apply {
        this.featureFactories = factories
      }

      internal var styleSelector = FeatureStyle.Selector { FeatureStyle.Strikethrough }

      override fun deprecationStyleSelector(selector: FeatureStyle.Selector): BuildingStep = apply {
        this.styleSelector = selector
      }

      internal var alignmentSelector = FeatureAlignment.Selector { FeatureAlignment.Bottom }

      override fun deprecationAlignmentSelector(selector: FeatureAlignment.Selector): BuildingStep =
        apply {
          this.alignmentSelector = selector
        }

      internal var offscreenSectionsBehavior: OffscreenSectionsBehavior = Unlimited

      override fun offscreenSectionBehavior(behavior: OffscreenSectionsBehavior): BuildingStep =
        apply {
          this.offscreenSectionsBehavior = behavior
        }

      override fun build(): Configuration = Configuration(this)
    }

    public companion object {
      /** Creates a [Configuration] using the given [Laboratory] instance and feature factories. */
      public fun create(
        laboratory: Laboratory,
        featureFactories: Map<String, FeatureFactory>,
      ): Configuration = builder().laboratory(laboratory).featureFactories(featureFactories).build()

      /**
       * Returns a new [Configuration] builder, allowing customization of optional settings before
       * creating the configuration.
       */
      public fun builder(): LaboratoryStep = Builder()
    }

    /**
     * First step in constructing a [Configuration]. This step requires providing a [Laboratory]
     * instance.
     */
    public interface LaboratoryStep {
      /**
       * Specifies the [Laboratory] instance to use. The provided instance should share the same
       * option storage as the application.
       */
      public fun laboratory(laboratory: Laboratory): FeatureFactoriesStep
    }

    /** Second step in the [Configuration] builder, requiring specification of feature factories. */
    public interface FeatureFactoriesStep {
      /**
       * Specifies the feature factories to include. Each entry in the map adds a section (tab) to
       * the inspector: the map key is the section title, and the [FeatureFactory] value provides
       * the feature flags for that section.
       */
      public fun featureFactories(factories: Map<String, FeatureFactory>): BuildingStep
    }

    /**
     * Final step in building a [Configuration], where optional settings can be provided before
     * creating the configuration.
     */
    public interface BuildingStep {
      /**
       * Sets a custom [FeatureStyle.Selector] to control the visual styling for deprecated feature
       * flags.
       */
      public fun deprecationStyleSelector(selector: FeatureStyle.Selector): BuildingStep

      /**
       * Sets a custom [FeatureAlignment.Selector] to control the ordering of deprecated feature
       * flags in their list.
       */
      public fun deprecationAlignmentSelector(selector: FeatureAlignment.Selector): BuildingStep

      /**
       * Configures how the inspector handles offscreen feature sections. Provide an
       * [OffscreenSectionsBehavior] to either keep all sections in memory or limit the number
       * retained.
       */
      public fun offscreenSectionBehavior(behavior: OffscreenSectionsBehavior): BuildingStep

      /** Builds and returns the [Configuration] instance with all specified settings. */
      public fun build(): Configuration
    }
  }

  public companion object {
    private const val featuresLabel = "Features"
    private var _configuration: Configuration? = null
    internal val configuration
      get() = requireNotNull(_configuration) { "Configuration was not initialized." }

    /**
     * Initializes the Laboratory inspector with a default "Features" section using the provided
     * [mainFactory]. Additional sections can be added via [externalFactories], a map of section
     * name to [FeatureFactory]. Call this to set up LaboratoryActivity with a custom configuration
     * before invoking [start].
     */
    public fun configure(
      laboratory: Laboratory,
      mainFactory: FeatureFactory,
      externalFactories: Map<String, FeatureFactory> = emptyMap(),
    ) {
      val filteredFactories = externalFactories.filterNot { it.key == featuresLabel }
      configure(
        Configuration.create(
          laboratory,
          featureFactories = mapOf(featuresLabel to mainFactory) + filteredFactories,
        )
      )
    }

    /**
     * Applies a pre-built [Configuration] for the Laboratory inspector. Call this to set up
     * LaboratoryActivity with a custom configuration before invoking [start].
     */
    public fun configure(configuration: Configuration) {
      this._configuration = configuration
    }

    /**
     * Launches the Laboratory inspector UI. Ensure that you have called one of the [configure]
     * methods beforehand to properly initialize the LaboratoryActivity.
     */
    public fun start(context: Context) {
      check(_configuration != null) {
        "${LaboratoryActivity::class.java} must be initialized before using it."
      }
      context.startActivity(Intent(context, LaboratoryActivity::class.java))
    }
  }
}
