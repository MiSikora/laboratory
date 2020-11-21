package io.mehow.laboratory.inspector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.mehow.laboratory.Feature
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class GroupFragment : Fragment() {
  private val sectionName
    get() = requireNotNull(requireArguments().getString(sectionKey)) {
      "Missing section key"
    }
  internal val viewModel by viewModels<GroupViewModel> {
    GroupViewModel.Factory(LaboratoryActivity.configuration, sectionName)
  }
  private val adapter = FeatureAdapter(object : FeatureAdapter.Listener {
    override fun onSelectFeature(feature: Feature<*>) {
      lifecycleScope.launch { viewModel.selectFeature(feature) }
    }

    override fun onSelectSource(feature: Feature<*>) {
      lifecycleScope.launch { viewModel.selectFeature(feature) }
    }
  })

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    inState: Bundle?,
  ): View = inflater.inflate(R.layout.io_mehow_laboratory_feature_group, container, false)

  override fun onViewCreated(view: View, inState: Bundle?) {
    val featureGroup = view.findViewById<RecyclerView>(R.id.io_mehow_laboratory_feature_group)
    featureGroup.layoutManager = LinearLayoutManager(requireActivity())
    featureGroup.adapter = adapter
    observeGroup()
  }

  private fun observeGroup() = viewModel
      .observeFeatureGroup()
      .onEach { adapter.submitList(it) }
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
