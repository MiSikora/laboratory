package io.mehow.laboratory.inspector

import android.view.View
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.annotation.IdRes
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.DynamicAnimation.ALPHA
import androidx.dynamicanimation.animation.DynamicAnimation.ROTATION
import androidx.dynamicanimation.animation.DynamicAnimation.ROTATION_X
import androidx.dynamicanimation.animation.DynamicAnimation.ROTATION_Y
import androidx.dynamicanimation.animation.DynamicAnimation.SCALE_X
import androidx.dynamicanimation.animation.DynamicAnimation.SCALE_Y
import androidx.dynamicanimation.animation.DynamicAnimation.SCROLL_X
import androidx.dynamicanimation.animation.DynamicAnimation.SCROLL_Y
import androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_X
import androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_Y
import androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_Z
import androidx.dynamicanimation.animation.DynamicAnimation.ViewProperty
import androidx.dynamicanimation.animation.DynamicAnimation.X
import androidx.dynamicanimation.animation.DynamicAnimation.Y
import androidx.dynamicanimation.animation.DynamicAnimation.Z
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.SpringForce.DAMPING_RATIO_NO_BOUNCY
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.absoluteValue

internal fun View.focusAndShowKeyboard() {
  fun View.showKeyboardIfFocused() {
    if (isFocused) post {
      context.getSystemService<InputMethodManager>()?.showSoftInput(this, SHOW_IMPLICIT)
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
  context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, HIDE_NOT_ALWAYS)
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

internal fun View.spring(
  property: ViewProperty,
  key: Int? = null,
  springBuilder: SpringForce.() -> Unit = {
    dampingRatio = DAMPING_RATIO_NO_BOUNCY
    stiffness = 500f
  },
): SpringAnimation {
  val springTag = key ?: property.springTag
  var springAnimation = getTag(springTag) as? SpringAnimation?
  if (springAnimation == null) {
    val springForce = SpringForce().apply { springBuilder() }
    springAnimation = SpringAnimation(this, property).apply { spring = springForce }
    setTag(springTag, springAnimation)
  }
  return springAnimation
}

@Suppress("VariableNaming") // "X" and "Y" suffixes make sense here.
internal fun View.springVisibility(
  isVisible: Boolean,
  springBuilder: SpringForce.() -> Unit = {
    dampingRatio = DAMPING_RATIO_NO_BOUNCY
    stiffness = 1_000f
  },
) {
  val targetScale = if (isVisible) 1f else 0f
  val hasTargetScale = scaleX == targetScale && scaleY == targetScale
  if (hasTargetScale && this.isVisible == isVisible) return
  val springScaleX = spring(SCALE_X, springBuilder = springBuilder)
  val springScaleY = spring(SCALE_Y, springBuilder = springBuilder)
  this.isVisible = true
  springScaleX.applyAnimationEndListener(
      view = this@springVisibility,
      previousAnimationTag = R.id.io_mehow_laboratory_animation_tag,
      doOnAnimationEnd = { if (!isVisible) isGone = true }
  ).animateToFinalPosition(targetScale)
  springScaleY.animateToFinalPosition(targetScale)
}

private fun SpringAnimation.applyAnimationEndListener(
  view: View,
  @IdRes previousAnimationTag: Int,
  doOnAnimationEnd: () -> Unit,
) = apply {
  val previousListener = view.getTag(previousAnimationTag) as DynamicAnimation.OnAnimationEndListener?
  removeEndListener(previousListener)
  val listener = DynamicAnimation.OnAnimationEndListener { _, _, _, _ -> doOnAnimationEnd() }
  view.setTag(previousAnimationTag, listener)
  addEndListener(listener)
}

private val ViewProperty.springTag
  get() = when (this) {
    TRANSLATION_X -> R.id.io_mehow_laboratory_translation_x_tag
    TRANSLATION_Y -> R.id.io_mehow_laboratory_translation_y_tag
    TRANSLATION_Z -> R.id.io_mehow_laboratory_translation_z_tag
    SCALE_X -> R.id.io_mehow_laboratory_scale_x_tag
    SCALE_Y -> R.id.io_mehow_laboratory_scale_y_tag
    ROTATION -> R.id.io_mehow_laboratory_rotation_tag
    ROTATION_X -> R.id.io_mehow_laboratory_rotation_x_tag
    ROTATION_Y -> R.id.io_mehow_laboratory_rotation_y_tag
    X -> R.id.io_mehow_laboratory_x_tag
    Y -> R.id.io_mehow_laboratory_y_tag
    Z -> R.id.io_mehow_laboratory_z_tag
    ALPHA -> R.id.io_mehow_laboratory_alpha_tag
    SCROLL_X -> R.id.io_mehow_laboratory_scroll_x_tag
    SCROLL_Y -> R.id.io_mehow_laboratory_scroll_y_tag
    else -> error("Property $this requires custom view tag key.")
  }
