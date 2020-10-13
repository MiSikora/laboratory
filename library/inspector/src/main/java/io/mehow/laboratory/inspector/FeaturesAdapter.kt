package io.mehow.laboratory.inspector

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class FeaturesAdapter(
  fragmentActivity: FragmentActivity,
  private val sections: List<String>,
) : FragmentStateAdapter(fragmentActivity) {
  override fun getItemCount(): Int = sections.size

  override fun createFragment(position: Int): Fragment {
    return FeatureGroupFragment.create(sections[position])
  }
}
