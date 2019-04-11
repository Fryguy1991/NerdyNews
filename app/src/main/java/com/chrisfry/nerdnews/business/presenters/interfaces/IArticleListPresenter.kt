package com.chrisfry.nerdnews.business.presenters.interfaces

import com.chrisfry.nerdnews.business.presenters.ArticleListPresenter

/**
 * Presenter interface for presenter that will display a list of articles
 */
interface IArticleListPresenter : IBasePresenter<ArticleListPresenter.IArticleListView> {

    /**
     * Attached view has requested article list for display
     */
    fun requestArticles()

    /**
     * Attached view has requested that more articles be provided (reached bottom of list)
     */
    fun requestMoreArticles()
}