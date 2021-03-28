package io.mehow.laboratory.inspector

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.mehow.laboratory.Feature
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class SectionFragment : Fragment(R.layout.io_mehow_laboratory_feature_group) {
  val inspectorViewModel by activityViewModels<InspectorViewModel> {
    InspectorViewModel.Factory(LaboratoryActivity.configuration, searchViewModel)
  }
  private val searchViewModel by activityViewModels<SearchViewModel> { SearchViewModel.Factory }
  private val sectionName get() = requireStringArgument(sectionKey)

  private lateinit var layoutManager: SmoothScrollingLinearLayoutManager
  private val featureAdapter = FeatureAdapter(object : FeatureAdapter.Listener {
    override fun onSelectOption(option: Feature<*>) = inspectorViewModel.selectFeature(option)

    override fun onGoToFeature(feature: Class<Feature<*>>) {
      lifecycleScope.launch {
        val coordinates = inspectorViewModel.goTo(feature)
        if (coordinates == null) {
          val root = requireActivity().findViewById<CoordinatorLayout>(R.id.io_mehow_laboratory_root)
          val message = getString(R.string.io_mehow_laboratory_feature_not_found, feature.simpleName)
          Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show()
        }
      }
    }
  })

  override fun onViewCreated(view: View, inState: Bundle?) {
    view.findViewById<RecyclerView>(R.id.io_mehow_laboratory_feature_section).apply {
      layoutManager = SmoothScrollingLinearLayoutManager(requireActivity()).also {
        this@SectionFragment.layoutManager = it
      }
      adapter = featureAdapter
      hideKeyboardOnScroll()
    }
    observeGroup()
  }

  private fun observeGroup() = inspectorViewModel.sectionFlow(sectionName)
      .onEach { featureAdapter.submitList(it) }
      .launchIn(viewLifecycleOwner.lifecycleScope)

  fun scrollTo(index: Int) = layoutManager.smoothScrollTo(index)

  companion object {
    private const val sectionKey = "Section.Key"

    fun create(section: String): SectionFragment {
      return SectionFragment().apply {
        arguments = bundleOf(sectionKey to section)
      }
    }
  }
}
