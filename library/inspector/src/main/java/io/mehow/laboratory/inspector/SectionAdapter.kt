package io.mehow.laboratory.inspector

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class SectionAdapter(
  activity: FragmentActivity,
  private val sectionNames: List<String>,
) : FragmentStateAdapter(activity) {
  override fun getItemCount() = sectionNames.size

  override fun createFragment(position: Int) = SectionFragment.create(sectionNames[position])
}
