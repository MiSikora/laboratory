package io.mehow.laboratory.inspector

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.delay
import java.lang.ref.WeakReference

internal class SectionAdapter(
  activity: FragmentActivity,
  private val sectionNames: List<String>,
) : FragmentStateAdapter(activity) {
  private val pages = mutableMapOf<String, WeakReference<SectionFragment>>()

  override fun getItemCount() = sectionNames.size

  override fun createFragment(position: Int): Fragment {
    val sectionName = sectionNames[position]
    return SectionFragment.create(sectionName).also {
      pages[sectionName] = WeakReference(it)
    }
  }

  suspend fun awaitSectionFragment(sectionName: String): SectionFragment = pages[sectionName]?.get() ?: run {
    delay(100) // ¯\_(ツ)_/¯
    awaitSectionFragment(sectionName)
  }
}
