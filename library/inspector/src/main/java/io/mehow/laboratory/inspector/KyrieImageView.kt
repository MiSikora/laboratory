package io.mehow.laboratory.inspector

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import io.mehow.laboratory.inspector.kyrie.KyrieDrawable

internal class KyrieImageView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {
  private var kyrieDrawable: KyrieDrawable? = null

  init {
    if (drawable::class in vectorDrawableTypes) {
      context.withStyledAttributes(attrs, R.styleable.AppCompatImageView, defStyleAttr, 0) {
        setImageResource(getResourceId(R.styleable.AppCompatImageView_srcCompat, -1))
      }
    }
  }

  override fun setImageResource(@DrawableRes resId: Int) {
    kyrieDrawable = KyrieDrawable.create(context, resId)?.also(::setImageDrawable)
  }

  @get:Keep @set:Keep
  var playTime
    get() = kyrieDrawable?.run { currentPlayTime.toFloat() / totalDuration } ?: 0f
    set(value) = kyrieDrawable?.run {
      val coercedValue = value.coerceIn(0f, 1f)
      currentPlayTime = (totalDuration * coercedValue).toLong()
    } ?: Unit

  private companion object {
    val vectorDrawableTypes = setOf(
        VectorDrawable::class,
        AnimatedVectorDrawable::class,
        VectorDrawableCompat::class,
        AnimatedVectorDrawableCompat::class,
    )
  }
}
