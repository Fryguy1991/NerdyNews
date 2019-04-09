package com.chrisfry.nerdnews.userinterface.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.InvalidPositionException
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.userinterface.adapters.holders.ArticleViewHolder
import com.chrisfry.nerdnews.userinterface.fragments.ArticleListFragment
import com.chrisfry.nerdnews.userinterface.interfaces.ArticleSelectionListener
import com.chrisfry.nerdnews.userinterface.interfaces.ItemSelectionListener
import com.chrisfry.nerdnews.utils.LogUtils

/**
 * Adapter for displaying Article objects in a recycler view
 */
class ArticleRecyclerViewAdapter(private val fragment: ArticleListFragment) : BaseRecyclerViewAdapter<ArticleDisplayModel, ArticleViewHolder>(),
    ItemSelectionListener {
    companion object {
        private val TAG = ArticleRecyclerViewAdapter::class.java.name
    }

    // Reference to object listening for article selection
    var listener: ArticleSelectionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_article, parent, false))
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        if (position < 0 || position >= itemCount) {
            throw InvalidPositionException("Position in ArticleRecyclerAdapter was invalid: $position")
        } else {
            val articleToDisplay = itemList[position]

            holder.setImageUrl(fragment, articleToDisplay.imageUrl)
            holder.setSourceName(articleToDisplay.sourceName)

            // Always display something in title field
            if (articleToDisplay.title.isEmpty()) {
                holder.setArticleTitle(fragment.getString(R.string.no_title_string))
            } else {
                holder.setArticleTitle(articleToDisplay.title)
            }

            holder.listener = this
        }
    }

    /**
     * Item in the recycler view was selected
     *
     * @param adapterPosition: Adapter index of the selected item
     */
    override fun onItemSelected(adapterPosition: Int) {
        if (adapterPosition < 0 || adapterPosition >= itemCount) {
            throw InvalidPositionException("$TAG: Invalid position received in onItemSelected")
        } else {
            LogUtils.debug(TAG, "Item selected with index: $adapterPosition")
            listener?.onArticleSelected(itemList[adapterPosition])
        }
    }
}