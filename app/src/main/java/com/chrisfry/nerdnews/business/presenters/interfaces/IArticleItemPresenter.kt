package com.chrisfry.nerdnews.business.presenters.interfaces

import com.chrisfry.nerdnews.business.presenters.ArticleItemPresenter
import com.chrisfry.nerdnews.model.ArticleDisplayModelParcelable

/**
 * Interface for presenter that displays a single article to a view
 */
interface IArticleItemPresenter: IBasePresenter<ArticleItemPresenter.IArticleItemView> {

    /**
     * Load presenter with article data it should display
     *
     * @param articleToDisplay: Article data to be displayed
     */
    fun setArticleData(articleToDisplay: ArticleDisplayModelParcelable?)

    /**
     * View has pressed the button to go to the article source URL
     */
    fun goToArticleClicked()
}