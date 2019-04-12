package com.chrisfry.nerdnews.userinterface.widgets

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chrisfry.nerdnews.R

class LinearLayoutDecorator : RecyclerView.ItemDecoration() {
    companion object {
        private val TAG = LinearLayoutDecorator::class.java.simpleName
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

            // Use default margin for top when first item in the adapter, else use half default margin
            outRect.top = when (childIndex) {
                0 -> defaultMarginPixels
                else -> halfDefaultMarginPixels
            }

            // Use default margin for bottom when last item in the adapter, else use half default margin
            outRect.bottom = when {
                totalChildCount > 0 && childIndex == totalChildCount - 1 -> defaultMarginPixels
                else -> halfDefaultMarginPixels
            }
        }
    }
}