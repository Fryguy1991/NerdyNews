package com.chrisfry.nerdnews.userinterface.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.InvalidPositionException
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.userinterface.adapters.holders.ArticleViewHolder
import com.chrisfry.nerdnews.userinterface.fragments.ArticleListFragment

/**
 * Adapter for displaying Article objects in a recycler view
 */
class ArticleRecyclerViewAdapter(private val fragment: ArticleListFragment) : BaseRecyclerViewAdapter<ArticleDisplayModel, ArticleViewHolder>() {
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

            if (articleToDisplay.title.isEmpty()) {
                holder.setArticleTitle(fragment.getString(R.string.no_title_string))
            } else {
                holder.setArticleTitle(articleToDisplay.title)
            }
        }
    }
}