package io.mehow.laboratory.inspector

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use
import com.github.alexjlockwood.kyrie.KyrieDrawable

internal class KyrieImageView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {
  private var kyrieDrawable: KyrieDrawable? = null

  init {
    if (drawable is VectorDrawable || drawable is AnimatedVectorDrawable) {
      @Suppress("CustomViewStyleable")
      context.obtainStyledAttributes(attrs, R.styleable.AppCompatImageView, defStyleAttr, 0).use {
        setImageResource(it.getResourceId(R.styleable.AppCompatImageView_srcCompat, -1))
      }
    }
  }

  override fun setImageResource(@DrawableRes resId: Int) {
    kyrieDrawable = KyrieDrawable.create(context, resId)?.also(::setImageDrawable)
  }

  @Keep
  var playTime: Float = 0f
    set(value) {
      field = kyrieDrawable?.run {
        val coercedValue = value.coerceIn(0f, 1f)
        currentPlayTime = (totalDuration * coercedValue).toLong()
        coercedValue
      } ?: 0f
    }
}
