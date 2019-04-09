package com.chrisfry.nerdnews.business.presenters.interfaces

import com.chrisfry.nerdnews.business.presenters.NewsPagingPresenter

/**
 * Interface for presenter that will provide article data
 */
interface INewsPagingPresenter : IBasePresenter<NewsPagingPresenter.INewsPagingView> {

    /**
     * Attached view has requested that the article list be refreshed
     */
    fun requestArticleRefresh()
}