package com.chrisfry.nerdnews.business.presenters.interfaces

import com.chrisfry.nerdnews.business.presenters.ArticleListPresenter

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