package io.mehow.laboratory.inspector

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import io.mehow.laboratory.Feature
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class SectionFragment : Fragment(R.layout.io_mehow_laboratory_feature_group) {
  val sectionName
    get() = requireStringArgument(sectionKey)

  val inspectorViewModel by
    activityViewModels<InspectorViewModel> {
      InspectorViewModel.Factory(LaboratoryActivity.configuration, searchViewModel)
    }
  private val searchViewModel by activityViewModels<SearchViewModel> { SearchViewModel.Factory }

  private lateinit var layoutManager: SmoothScrollingLinearLayoutManager
  private val featureAdapter =
    FeatureAdapter(
      object : FeatureAdapter.Listener {
        override fun onSelectOption(option: Feature<*>) = inspectorViewModel.selectFeature(option)
      }
    )

  override fun onViewCreated(view: View, inState: Bundle?) {
    view.findViewById<RecyclerView>(R.id.io_mehow_laboratory_feature_section).apply {
      layoutManager =
        SmoothScrollingLinearLayoutManager(requireActivity()).also {
          this@SectionFragment.layoutManager = it
        }
      adapter = featureAdapter
      hideKeyboardOnScroll()
      doOnApplyWindowInsets { view, insets, padding ->
        val bars =
          insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
          )
        view.updatePadding(bottom = padding.bottom + bars.bottom)
      }
    }
    observeGroup()
  }

  private fun observeGroup() =
    inspectorViewModel
      .sectionFlow(sectionName)
      .onEach { featureAdapter.submitList(it) }
      .launchIn(viewLifecycleOwner.lifecycleScope)

  fun scrollTo(index: Int) = layoutManager.smoothScrollTo(index)

  companion object {
    private const val sectionKey = "Section.Key"

    fun create(section: String): SectionFragment {
      return SectionFragment().apply {
        arguments = Bundle().apply { putString(sectionKey, section) }
      }
    }
  }
}
