package io.mehow.laboratory.inspector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch

internal class FeaturesFragment : Fragment() {
  private val viewModel by activityViewModels<FeaturesViewModel> {
    FeaturesViewModel.Factory(LaboratoryActivity.configuration)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    inState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.io_mehow_laboratory_features, container, false)
  }

  override fun onViewCreated(view: View, inState: Bundle?) {
    super.onViewCreated(view, inState)
    val groupName = requireNotNull(requireArguments().getString(groupNameKey)) {
      "Missing group name key"
    }
    val container = view.findViewById<ViewGroup>(R.id.io_mehow_laboratory_container)
    val spacing = resources.getDimensionPixelSize(R.dimen.io_mehow_laboratory_spacing)
    lifecycleScope.launch {
      for (group in viewModel.getFeatureGroups(groupName)) {
        val groupLabel = createFeatureGroupLabel(group, spacing)
        val groupView = FeatureGroupView(requireActivity(), group) { feature ->
          lifecycleScope.launch { viewModel.selectFeature(feature) }
        }
        container.addView(groupLabel)
        container.addView(groupView)
      }
    }
  }

  private fun createFeatureGroupLabel(group: FeatureGroup, spacing: Int): TextView {
    return MaterialTextView(requireActivity()).apply {
      layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
        setMargins(spacing, spacing, spacing, 0)
      }
      TextViewCompat.setTextAppearance(this, R.style.TextAppearance_MaterialComponents_Headline5)
      text = group.name
    }
  }

  companion object {
    private const val groupNameKey = "GroupName.Key"

    fun create(groupName: String): FeaturesFragment {
      return FeaturesFragment().apply {
        arguments = bundleOf(groupNameKey to groupName)
      }
    }
  }
}
