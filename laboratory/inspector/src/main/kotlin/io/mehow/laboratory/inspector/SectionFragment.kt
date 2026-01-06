package io.mehow.laboratory.inspector

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.mehow.laboratory.Feature
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class SectionFragment : Fragment(R.layout.io_mehow_laboratory_feature_group) {
  private val sectionName
    get() = requireArguments().getString(sectionKey)!!

  val toolbarViewModel by
    activityViewModels<ToolbarViewModel> {
      ToolbarViewModel.Factory(LaboratoryActivity.configuration)
    }

  val inspectionViewModel by
    activityViewModels<InspectionViewModel> {
      InspectionViewModel.Factory(LaboratoryActivity.configuration, toolbarViewModel.searchQueries)
    }

  override fun onViewCreated(view: View, inState: Bundle?) {
    val featureAdapter =
      FeatureAdapter(
        LaboratoryActivity.configuration.laboratory,
        viewLifecycleOwner.lifecycle,
        object : FeatureAdapter.Listener {
          override fun onSelectOption(option: Feature<*>) =
            inspectionViewModel.selectFeature(option)

          override fun onSelectSource(feature: Class<Feature<*>>, source: Feature.Source) =
            inspectionViewModel.selectSource(feature, source)
        },
      )

    view.findViewById<RecyclerView>(R.id.io_mehow_laboratory_feature_section).apply {
      layoutManager = LinearLayoutManager(requireActivity())
      adapter = featureAdapter
      hideKeyboardOnScroll()
      doOnApplyWindowInsets { view, insets, padding ->
        val bars = insets.getTopInsets()
        view.updatePadding(bottom = padding.bottom + bars.bottom)
      }
    }
    observeGroup(featureAdapter)
  }

  private fun observeGroup(adapter: FeatureAdapter) =
    inspectionViewModel
      .sectionFlow(sectionName)
      .onEach(adapter::submitList)
      .launchIn(viewLifecycleOwner.lifecycleScope)

  companion object {
    private const val sectionKey = "Section.Key"

    fun create(section: String): SectionFragment {
      return SectionFragment().apply {
        arguments = Bundle().apply { putString(sectionKey, section) }
      }
    }
  }
}
