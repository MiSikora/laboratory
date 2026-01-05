package io.mehow.laboratory.inspector

import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.absoluteValue

internal fun View.focusAndShowKeyboard() {
  fun View.showKeyboardIfFocused() {
    if (isFocused) {
      post {
        val service = context.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        service?.showSoftInput(this, SHOW_IMPLICIT)
      }
    }
  }

  requestFocus()
  if (!hasWindowFocus()) {
    val listener =
      object : ViewTreeObserver.OnWindowFocusChangeListener {
        override fun onWindowFocusChanged(hasFocus: Boolean) {
          viewTreeObserver.removeOnWindowFocusChangeListener(this)
          if (hasFocus) showKeyboardIfFocused()
        }
      }
    viewTreeObserver.addOnWindowFocusChangeListener(listener)
  } else {
    showKeyboardIfFocused()
  }
}

internal fun View.hideKeyboard() {
  clearFocus()
  val service = context.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
  service?.hideSoftInputFromWindow(windowToken, HIDE_NOT_ALWAYS)
}

internal fun RecyclerView.hideKeyboardOnScroll() {
  val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
  var totalDy = 0
  addOnScrollListener(
    object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        totalDy += dy.absoluteValue
        if (totalDy >= touchSlop) {
          totalDy = 0
          hideKeyboard()
        }
      }
    }
  )
}

// TODO: Set this from XML. https://issuetracker.google.com/issues/134912610
internal fun ViewPager2.disableScrollEffect() {
  (getChildAt(0) as? RecyclerView)?.overScrollMode = OVER_SCROLL_NEVER
}

internal fun View.doOnApplyWindowInsets(block: (View, WindowInsetsCompat, InitialPadding) -> Unit) {
  val initialPadding = initialPadding()
  ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
    block(view, insets, initialPadding)
    insets
  }
}

internal fun WindowInsetsCompat.getTopInsets() =
  getInsets(Type.systemBars() or Type.displayCutout())

internal data class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

private fun View.initialPadding() =
  InitialPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
