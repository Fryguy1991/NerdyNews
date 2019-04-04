package com.chrisfry.nerdnews.userinterface.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.InvalidPositionException
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.userinterface.adapters.holders.ArticleViewHolder
import com.chrisfry.nerdnews.userinterface.fragments.NewsListFragment

/**
 * Adapter for displaying Article objects in a recycler view
 */
class ArticleRecyclerViewAdapter(private val fragment: NewsListFragment) : BaseRecyclerViewAdapter<Article, ArticleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_article, parent, false))
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        if (position < 0 || position >= itemCount) {
            throw InvalidPositionException("Position in ArticleRecyclerAdapter was invalid: $position")
        } else {
            val articleToDisplay = itemList[position]

            holder.setImageUrl(fragment, articleToDisplay.urlToImage)
            holder.setSourceName(articleToDisplay.source.name)

            if (articleToDisplay.title == null) {
                holder.setArticleTitle(fragment.getString(R.string.no_title_string))
            } else {
                holder.setArticleTitle(articleToDisplay.title)
            }
        }
    }
}