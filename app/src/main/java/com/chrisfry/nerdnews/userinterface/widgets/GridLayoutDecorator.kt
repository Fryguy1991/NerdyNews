package com.chrisfry.nerdnews.userinterface.widgets

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.R

class GridLayoutDecorator : RecyclerView.ItemDecoration() {
    companion object {
        private val TAG = GridLayoutDecorator::class.java.name
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val parentResources = parent.resources
        if (parentResources != null) {
            val halfDefaultMarginPixels = parentResources.getDimensionPixelOffset(R.dimen.half_default_margin)
            val defaultMarginPixels = parentResources.getDimensionPixelOffset(R.dimen.default_margin)

            val childIndex = parent.getChildAdapterPosition(view)

            // Retrieve item count from recyclerview adapter
            var totalChildCount = -1
            val parentAdapter = parent.adapter
            if (parentAdapter != null) {
                totalChildCount = parentAdapter.itemCount
            }

            // Use default margin for top items in the adapter, else use half default margin
            outRect.top = when (childIndex < AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT) {
                true -> defaultMarginPixels
                else -> halfDefaultMarginPixels
            }

            // Use default margin for bottom items in the adapter, else use half default margin
            outRect.bottom = when (childIndex > totalChildCount - AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT) {
                true -> defaultMarginPixels
                false -> halfDefaultMarginPixels
            }

            // Assign left margin to default. Last column right margin is handled by recycler view layout padding
            outRect.left = defaultMarginPixels
        }
    }
}