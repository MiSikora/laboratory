package io.mehow.laboratory.inspector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.mehow.laboratory.Feature
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class FeatureGroupFragment : Fragment() {
  internal val viewModel by activityViewModels<FeaturesViewModel> {
    FeaturesViewModel.Factory(LaboratoryActivity.configuration)
  }
  private val adapter = FeatureGroupAdapter(object : FeatureGroupAdapter.Listener {
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
    val features = view.findViewById<RecyclerView>(R.id.io_mehow_laboratory_features)
    features.layoutManager = LinearLayoutManager(requireActivity())
    features.adapter = adapter
    observeFeatureGroups()
  }

  private fun observeFeatureGroups() {
    val section = requireNotNull(requireArguments().getString(sectionKey)) {
      "Missing section key"
    }
    viewModel
        .observeFeatureGroups(section)
        .onEach { adapter.submitList(it) }
        .launchIn(lifecycleScope)
  }

  companion object {
    private const val sectionKey = "Section.Key"

    fun create(section: String): FeatureGroupFragment {
      return FeatureGroupFragment().apply {
        arguments = bundleOf(sectionKey to section)
      }
    }
  }
}
