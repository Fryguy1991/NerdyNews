package com.chrisfry.nerdnews.userinterface.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * ViewPager subclass which allows paging to be disabled
 */
class SwipeRefreshViewPager : ViewPager {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    var pagingEnabled = true

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        // Only handle touch events if ViewPager is enabled
        return pagingEnabled && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        // Only intercept touch events if ViewPager is enabled
        return pagingEnabled && super.onInterceptTouchEvent(ev)
    }
}