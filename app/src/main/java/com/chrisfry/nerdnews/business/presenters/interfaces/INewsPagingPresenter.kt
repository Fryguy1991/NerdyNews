package com.chrisfry.nerdnews.business.presenters.interfaces

import com.chrisfry.nerdnews.business.presenters.NewsPagingPresenter

/**
 * Interface for presenter that will provide article data
 */
interface INewsPagingPresenter : IBasePresenter<NewsPagingPresenter.INewsPagingView> {

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
}