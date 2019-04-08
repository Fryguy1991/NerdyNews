package com.chrisfry.nerdnews.userinterface.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Class for displaying an image where height always matches width
 */
class SquareImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}