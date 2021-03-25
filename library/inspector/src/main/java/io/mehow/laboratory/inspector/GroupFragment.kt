package io.mehow.laboratory.inspector

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.mehow.laboratory.Feature
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class GroupFragment : Fragment(R.layout.io_mehow_laboratory_feature_group) {
  private val sectionName get() = requireStringArgument(sectionKey)
  private val searchViewModel by activityViewModels<SearchViewModel> { SearchViewModel.Factory }
  val viewModel by viewModels<InspectorViewModel> {
    InspectorViewModel.Factory(LaboratoryActivity.configuration, searchViewModel)
  }
  private val featureAdapter = FeatureAdapter(object : FeatureAdapter.Listener {
    override fun onSelectFeature(feature: Feature<*>) {
      viewModel.selectFeature(feature)
    }

    override fun onSelectSource(feature: Feature<*>) = viewModel.selectFeature(feature)
  })

  override fun onViewCreated(view: View, inState: Bundle?) {
    view.findViewById<RecyclerView>(R.id.io_mehow_laboratory_feature_group).apply {
      layoutManager = LinearLayoutManager(requireActivity())
      adapter = featureAdapter
      hideKeyboardOnScroll()
    }
    observeGroup()
  }

  private fun observeGroup() = viewModel.sectionFlow(sectionName)
      .onEach { featureAdapter.submitList(it) }
      .launchIn(viewLifecycleOwner.lifecycleScope)

  companion object {
    private const val sectionKey = "Section.Key"

    fun create(section: String): GroupFragment {
      return GroupFragment().apply {
        arguments = bundleOf(sectionKey to section)
      }
    }
  }
}
