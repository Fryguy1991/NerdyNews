package com.chrisfry.nerdnews.userinterface.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chrisfry.nerdnews.R
import java.lang.Exception

class LinearLayoutDecorator(context: Context) : RecyclerView.ItemDecoration() {
    companion object {
        private val TAG = LinearLayoutDecorator::class.java.name
    }
    // Drawable for divider
    private val mDividerDrawable: Drawable

    init {
        val drawable = ContextCompat.getDrawable(context, R.drawable.decorator_linear_layout)
        if (drawable != null) {
            mDividerDrawable = drawable
            mDividerDrawable.alpha = 255 / 4
        } else {
            throw Exception("$TAG: Failed to retrieve divider drawable")
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val context = parent.context

        // Retrieve side margin value from resources
        var sideMargin = 0
        if (context != null) {
            sideMargin = context.resources.getDimensionPixelOffset(R.dimen.default_margin)
        }

        val leftBound = parent.paddingLeft + sideMargin
        val rightBound = parent.width - parent.paddingRight - sideMargin

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val topBound = child.bottom + params.bottomMargin
            val bottomBound = topBound + mDividerDrawable.intrinsicHeight

            mDividerDrawable.setBounds(leftBound, topBound, rightBound, bottomBound)
            mDividerDrawable.draw(canvas)
        }
    }
}