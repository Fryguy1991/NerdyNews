package com.chrisfry.nerdnews.userinterface.interfaces

import com.chrisfry.nerdnews.model.ArticleDisplayModelParcelable

/**
 * Listener for when an article is selected
 */
interface ArticleSelectionListener {
    /**
     * Event fired when an article is selected
     *
     * @param article: Article data for the selected article
     */
    fun onArticleSelected(article: ArticleDisplayModelParcelable)
}