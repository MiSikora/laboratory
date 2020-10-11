package io.mehow.laboratory.inspector

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class FeaturesAdapter(
  fragmentActivity: FragmentActivity,
  private val groupNames: List<String>,
) : FragmentStateAdapter(fragmentActivity) {
  override fun getItemCount(): Int = groupNames.size

  override fun createFragment(position: Int): Fragment {
    return FeatureGroupFragment.create(groupNames[position])
  }
}
