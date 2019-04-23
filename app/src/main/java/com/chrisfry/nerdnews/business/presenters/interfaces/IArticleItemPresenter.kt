package com.chrisfry.nerdnews.business.presenters.interfaces

import com.chrisfry.nerdnews.business.presenters.ArticleItemPresenter

/**
 * Interface for presenter that displays a single article to a view
 */
interface IArticleItemPresenter: IBasePresenter<ArticleItemPresenter.IArticleItemView> {

    /**
     * View has pressed the button to go to the article source URL
     */
    fun goToArticleClicked()
}