package io.mehow.laboratory.inspector

import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.absoluteValue

internal fun View.focusAndShowKeyboard() {
  fun View.showKeyboardIfFocused() {
    if (isFocused) post {
      val service = context.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
      service?.showSoftInput(this, SHOW_IMPLICIT)
    }
  }

  requestFocus()
  if (!hasWindowFocus()) {
    val listener = object : ViewTreeObserver.OnWindowFocusChangeListener {
      override fun onWindowFocusChanged(hasFocus: Boolean) {
        viewTreeObserver.removeOnWindowFocusChangeListener(this)
        if (hasFocus) showKeyboardIfFocused()
      }
    }
    viewTreeObserver.addOnWindowFocusChangeListener(listener)
  } else showKeyboardIfFocused()
}

internal fun View.hideKeyboard() {
  clearFocus()
  val service = context.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
  service?.hideSoftInputFromWindow(windowToken, HIDE_NOT_ALWAYS)
}

internal fun RecyclerView.hideKeyboardOnScroll() {
  val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
  var totalDy = 0
  addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      totalDy += dy.absoluteValue
      if (totalDy >= touchSlop) {
        totalDy = 0
        hideKeyboard()
      }
    }
  })
}

internal var View.isVisible: Boolean
  get() = visibility == VISIBLE
  set(value) {
    visibility = if (value) VISIBLE else GONE
  }

internal var View.isGone: Boolean
  get() = visibility == GONE
  set(value) {
    visibility = if (value) GONE else VISIBLE
  }
