package com.chrisfry.nerdnews.userinterface.widgets

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.R

class GridLayoutDecorator : RecyclerView.ItemDecoration() {
    companion object {
        private val TAG = GridLayoutDecorator::class.java.simpleName
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val parentResources = parent.resources
        if (parentResources != null) {
            val halfDefaultMarginPixels = parentResources.getDimensionPixelOffset(R.dimen.half_default_margin)
            val defaultMarginPixels = parentResources.getDimensionPixelOffset(R.dimen.default_margin)

            val childIndex = parent.getChildAdapterPosition(view)

            // Retrieve item count from recyclerview adapter
            val totalChildCount: Int
            val parentAdapter = parent.adapter
            if (parentAdapter != null) {
                totalChildCount = parentAdapter.itemCount
            } else {
                throw Exception("$TAG: Error decorator does not have a parent adapter")
            }

            // Use default margin for top items in the adapter, else use half default margin
            outRect.top = if (childIndex < AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT) {
                defaultMarginPixels
            } else {
                halfDefaultMarginPixels
            }

            // Total number of rows
            val rowCount: Int = if (totalChildCount % AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT > 0) {
                (totalChildCount / AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT) + 1
            } else {
                totalChildCount / AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT
            }

            // First index in last row = (row count - 1) * column count
            val firstIndexInLastRow = (rowCount - 1) * AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT

            // Use default margin for bottom items in the adapter, else use half default margin
            outRect.bottom = if (childIndex >= firstIndexInLastRow) {
                defaultMarginPixels
            } else {
                halfDefaultMarginPixels
            }

            // Assign left margin to default. Last column right margin is handled by recycler view layout padding
            outRect.left = defaultMarginPixels
        }
    }
}