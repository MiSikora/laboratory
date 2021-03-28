package io.mehow.laboratory.inspector

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller

internal class SmoothScrollingLinearLayoutManager(
  context: Context,
) : LinearLayoutManager(context) {
  private val scroller = object : LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference() = SNAP_TO_START

    override fun getHorizontalSnapPreference() = SNAP_TO_START
  }

  fun smoothScrollTo(index: Int) {
    scroller.targetPosition = index
    startSmoothScroll(scroller)
  }
}
