package com.chrisfry.nerdnews.business.presenters.interfaces

import com.chrisfry.nerdnews.business.presenters.NewsListPresenter

/**
 * Interface for presenter that will provide article data
 */
interface INewsListPresenter : IBasePresenter<NewsListPresenter.INewsListView> {

    /**
     * Attached view has moved to page with provided index
     *
     * @param pageIndex: Index of the page the view has moved to
     */
    fun movedToPage(pageIndex: Int)

    /**
     * Attached view has requested that the article list be refreshed
     */
    fun requestArticleRefresh()

    /**
     * Attached view has requested that more articles be provided (reached bottom of list)
     */
    fun requestMoreArticles()
}